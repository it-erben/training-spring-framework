---
marp: true
theme: default
header: Spring Boot Advanced
footer: Alexander Erben
paginate: true
---
<!-- Dichte-Stufen gegen Folienueberlauf, siehe tools/check-slide-overflow.mjs -->
<style>
section.dense { font-size: 24.5px; }
</style>

# Batch im Betrieb

---

## In diesem Modul

* Zwei Betriebsmodelle: Artefakt und Dienst
* Idempotenz: denselbe Lauf zweimal durchführen
* Doppelstarts und Sperren
* Parallele Steps
* Metriken, Alarme und Metadatentabellen
* Schema und Migration

---

In Produktion kommen für Batch noch einige Fragen dazu

* Was passiert, wenn er ein zweites Mal läuft?
* Was passiert, wenn er zweimal gleichzeitig läuft?
* Was bleibt liegen, wenn der Prozess mitten im Lauf stirbt?
* Woran sieht jemand um drei Uhr nachts, dass etwas nicht stimmt?

---

Keine der vier Fragen beantwortet Spring Batch für euch. Das Framework liefert
aber die Werkzeuge: Metadaten über jeden Lauf, Wiederaufnahme ab dem letzten
Commit, Metriken.

Der Rest dieses Moduls geht die vier Fragen der Reihe nach durch, jede mit
einer Vorführung.

---

# Idempotenz

---

## Wiederholbar ist nicht wiederholungssicher

Spring Batch garantiert, dass ein **Restart** dort weitermacht, wo der letzte
Commit war. Was es nicht garantiert: dass ein zweiter vollständiger Lauf
denselben Zustand herstellt wie der erste.

* Ein neuer `JobParameter` erzeugt eine neue `JobInstance`. Der Lauf startet, egal was beim letzten Mal geschah.
* Was der Job dann in die Datenbank schreibt, ist seine eigene Sache.

---

## Vorführung: derselbe Import zweimal

```bash
curl -X POST 'localhost:8080/jobs/order-import?lauf=1'   # COMPLETED, je Step 6 gelesen und geschrieben
curl -X POST 'localhost:8080/jobs/order-import?lauf=2'   # FAILED, 2 gelesen, 0 geschrieben
```

Der zweite Lauf ist eine neue `JobInstance` und scheitert trotzdem: Der
Writer setzt `insert` ab, der Primärschlüssel ist belegt, die
`DuplicateKeyException` trifft schon den ersten Chunk.

---

## Pattern für Idempotenz

* **Upsert.** `merge into` statt `insert`.
* **Ladefenster löschen.** Ein vorgeschalteter Step räumt den Bereich auf, den der Lauf neu schreibt.
* **Fachlicher Schlüssel plus Prüfung.** Der Job überspringt, was schon da ist. Verlagert die Entscheidung in den Processor.

---

## Informationen aus dem Reader

Der `ExecutionContext` des Readers speichert bei jedem Commit,
wie weit der Reader gekommen ist. Beim Restart setzt er dort wieder auf.
Ein wiederaufgenommener Lauf liest die bereits verarbeiteten Zeilen
deshalb nicht erneut.

* Das gilt für Reader, die ihren Zustand melden. Ein selbstgebauter Reader ohne `ItemStream` fängt nach einem Restart von vorn an.
* Abschalten lässt sich das über `saveState(false)`. Sinnvoll nur, wenn der Lauf ohnehin nie wiederaufgenommen wird.

---

# Umgang mit Abstürzen

---

Stirbt die JVM mitten im Step, schreibt niemand mehr in die Metadaten. Die
`JobExecution` steht auf `STARTED` und bleibt dort stehen. Für die Datenbank
sieht ein abgestürzter Lauf genauso aus wie einer, der gerade arbeitet.

Spring Batch kann den Unterschied nicht erkennen. Es gibt kein Signal, das
"dieser Prozess lebt nicht mehr" bedeutet.

---

## Aufräumen und weitermachen

```bash
curl localhost:8080/jobs/running                          # Status STARTED, obwohl nichts läuft
curl -X POST 'localhost:8080/jobs/order-import?lauf=1'    # 409, die Instanz gilt als aktiv
curl -X POST localhost:8080/jobs/1/recover                # Status FAILED
curl -X POST localhost:8080/jobs/1/restart                # nimmt ab dem letzten Commit auf
```

Der wiederaufgenommene Step liest vier statt sechs Zeilen: Die beiden aus dem
ersten Chunk sind committet und stehen im `ExecutionContext`. Der zweite Step
lief noch gar nicht und verarbeitet alle sechs. Danach liegen zwölf Zeilen in
der Tabelle, keine doppelt.

---

<!-- _class: dense -->
## Nützliche Methoden für Recovery

| Aufruf                                         | Wirkung                                                           |
|------------------------------------------------|-------------------------------------------------------------------|
| `JobRepository.findRunningJobExecutions(name)` | alles, was als laufend gilt                                       |
| `JobOperator.recover(execution)`               | setzt eine verwaiste Execution auf `FAILED`                       |
| `JobOperator.abandon(execution)`               | markiert sie als aufgegeben, kein Restart mehr                    |
| `JobOperator.restart(execution)`               | neue Execution derselben Instanz                                  |
| `JobOperator.stop(id)`                         | bittet einen echten Lauf, an der nächsten Chunk-Grenze aufzuhören |

---

## Betriebsregeln

Ein Startskript, das nicht prüft, ob noch etwas läuft, produziert genau zwei
Fehlerbilder: einen blockierten Neustart nach jedem Absturz oder zwei
gleichzeitige Läufe.

Vor dem Start: laufende Executions abfragen. Steht dort etwas, ist die Frage nicht "starten oder nicht", sondern "lebt der Prozess dazu noch?"

---

# Parallele Steps

---

Ein Job, der zwei Dateien einliest, macht das nacheinander, weil `next` die
Reihenfolge festlegt. Gebraucht wird die Reihenfolge nur, wenn der zweite Step
auf dem Ergebnis des ersten aufsetzt.

* Zwei Importe aus getrennten Quellen brauchen sie nicht.
* Ein Bericht, der die importierten Daten liest, braucht sie sehr wohl.

Die Frage vor jeder Parallelisierung ist deshalb nicht "geht das technisch",
sondern "hängt der eine Step am Ergebnis des anderen".

---

## Split

```java
Flow flowA = new FlowBuilder<SimpleFlow>("flowA").start(importStep).build();
Flow flowB = new FlowBuilder<SimpleFlow>("flowB").start(importStepB).build();

return new JobBuilder(JOB_NAME, jobRepository)
        .start(flowA)
        .split(stepTaskExecutor)
        .add(flowB)
        .end()
        .build();
```

Statt einer Kette aus Steps beschreibt der Job zwei _Flows_, die ein
`TaskExecutor` gleichzeitig ausführt. Der Job gilt erst als fertig, wenn beide
Flows durch sind, und scheitert, sobald einer scheitert.

---

## Vorführung: nacheinander gegen gleichzeitig

| Lauf                            | `spring.batch.job` |
|---------------------------------|--------------------|
| `next`, zwei Steps nacheinander | 4,99 s             |
| `split`, dieselben zwei Steps   | 2,50 s             |

Zwölf Zeilen stehen danach in beiden Fällen in der Tabelle, sechs je Step. Die
Wartezeit im Processor steht für einen langsamen Fremdaufruf: Genau dort, wo
ein Step wartet statt rechnet, bringt Parallelität etwas.

---

## Mögliche Probleme bei Parallelität

* **Gemeinsame Daten.** Beide Steps dieser Demo schreiben in dieselbe Tabelle, aber auf getrennte Schlüsselbereiche. Haben wir das nicht, kann es zu Race Conditions kommen.
* **Transaktionen.** Jeder Flow hat seine eigenen Transaktionen. Ein Rollback im einen macht nichts rückgängig, was der andere schon committet hat.
* **Last.** Zwei Steps bedeutet doppelt so viele Verbindungen, doppelt so viele Schreibzugriffe und doppelt so viele Metadaten-Updates. Der Connection Pool kann limitieren.
* **Restart.** Ein wiederaufgenommener Lauf überspringt die Flows, die beim letzten Mal durchgelaufen sind und nimmt nur die gescheiterten wieder auf.

---

# Monitoring von Spring Batch

---

## Metriken

```bash
curl localhost:8080/actuator/metrics/spring.batch.job
```

```json
{"name":"spring.batch.job","baseUnit":"seconds",
 "availableTags":[{"tag":"spring.batch.job.name","values":["orderImportJob"]},
                  {"tag":"spring.batch.job.status","values":["COMPLETED"]}],
 "measurements":[{"statistic":"COUNT","value":1.0},
                 {"statistic":"TOTAL_TIME","value":2.505191}]}
```

`spring.batch.step` liefert dasselbe je Step, zusätzlich mit dem Step-Typ.

---

## Wonach man alarmiert

* **Ausgang.** Ein Lauf mit Status `FAILED`. Das ist der offensichtlichstee Alarm und der am schnellsten eingerichtete.
* **Ausbleiben.** Kein Lauf im erwarteten Fenster. Der Alarm, der am häufigsten fehlt. Ein Job, der gar nicht erst startet, meldet sich nie von selbst.
* **Laufzeit.** Zeigt wachsende Datenmengen an, bevor das Zeitfenster verfehlt wird.
* **Ausschussquote.** Mehr übersprungene Zeilen als üblich. Zeigt, dass sich am Liefersystem etwas geändert hat.

---

## Code

* `demos/sb-advanced-batch-operations-demo`: Dienst mit Job-Endpunkten, fünf Szenarien über Schalter reproduzierbar. Die README führt sie der Reihe nach vor.
* Zum Vergleich das Batch-Artefakt aus Modul 18: `demos/sb-advanced-batch-demo`.
