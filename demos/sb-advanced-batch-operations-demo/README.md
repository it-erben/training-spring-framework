# Batch-Betrieb-Demo (Modul 19)

Vorführdemo zu Modul *19 Batch im Betrieb*. Sie zeigt fünf Szenarien eines
Batch-Jobs im laufenden Betrieb: einen Import, der sich nicht wiederholen
lässt, einen Absturz mitten im Lauf, Doppelstarts, zwei Steps im Split und die
Frage, woran man von außen sieht, was passiert ist.

Anders als `sb-advanced-batch-demo` ist dieses Modul kein Batch-Artefakt,
sondern ein Dienst: Die Anwendung bleibt stehen, Jobs werden über HTTP
ausgelöst, und `spring.batch.job.enabled` steht auf `false`.

## Aufbau

Der Job `orderImportJob` besteht aus zwei gleichartigen Steps: CSV lesen,
verzögert verarbeiten, nach `book_order` schreiben, Chunk-Größe 2.
`importStep` liest `orders.csv` mit den Schlüsseln `O-*`, `importStepB` liest
`orders-b.csv` mit den Schlüsseln `P-*`. Die getrennten Schlüsselbereiche sind
die Voraussetzung dafür, dass beide gleichzeitig laufen dürfen. Die
Verzögerung im Processor steht für einen langsamen Fremdaufruf und hält den
Lauf lang genug, um ihm zusehen zu können.

Datenquelle ist dateibasiertes H2 unter `./target/operations`. Das
`JobRepository` überlebt damit den simulierten Absturz.

## Konfiguration

| Property | Vorgabe | Wirkung |
| --- | --- | --- |
| `ops.writer` | `insert` | `insert` schreibt stur ein, `upsert` führt über `merge into` zusammen |
| `ops.halt-after-chunk` | `0` | größer als 0 tötet die JVM nach diesem Chunk |
| `ops.lock` | `false` | prüft vor dem Start die Sperrtabelle `job_lock` |
| `ops.input` | `orders.csv` | Datei für `importStep` |
| `ops.input-b` | `orders-b.csv` | Datei für `importStepB` |
| `ops.parallel` | `false` | `true` verbindet die beiden Steps über einen Split statt über `next` |
| `ops.item-delay` | `400ms` | Wartezeit je Item |

## Endpunkte

| Endpunkt | Zweck |
| --- | --- |
| `POST /jobs/order-import?lauf={wert}` | startet den Job; `lauf` ist ein identifizierender Job-Parameter |
| `GET /jobs/running` | laufende Executions |
| `GET /jobs/{id}` | Zusammenfassung einer Execution mit Step-Zählern |
| `POST /jobs/{id}/recover` | räumt eine hängengebliebene Execution auf |
| `POST /jobs/{id}/restart` | nimmt einen gescheiterten Lauf wieder auf |

## Starten

```bash
mvn -q -DskipTests package
java -jar target/sb-advanced-batch-operations-demo-1.0.0-SNAPSHOT.jar
```

Zwischen den Szenarien `target/operations.mv.db` löschen, sonst wirken die
Daten des vorigen Laufs nach.

## Szenario 1: Der Lauf lässt sich nicht wiederholen

```bash
curl -X POST 'localhost:8080/jobs/order-import?lauf=1'
curl localhost:8080/jobs/1
```

Der erste Lauf endet `COMPLETED`: je Step sechs Zeilen gelesen und
geschrieben, drei Commits bei Chunk-Größe 2. Ein zweiter Lauf mit neuem
Parameter erzeugt eine neue JobInstance und scheitert trotzdem:

```bash
curl -X POST 'localhost:8080/jobs/order-import?lauf=2'
curl localhost:8080/jobs/2
```

`FAILED`, zwei gelesen, null geschrieben: Die `DuplicateKeyException` trifft
schon den ersten Chunk von `importStep`, `importStepB` läuft gar nicht erst
an. Neu gestartet mit `--ops.writer=upsert` endet derselbe Lauf
`COMPLETED`. Restart-Fähigkeit auf Job-Ebene und Idempotenz auf Fachebene sind
zwei verschiedene Dinge.

## Szenario 2: Absturz mitten im Lauf

```bash
java -jar target/sb-advanced-batch-operations-demo-1.0.0-SNAPSHOT.jar --ops.halt-after-chunk=2
curl -X POST 'localhost:8080/jobs/order-import?lauf=1'
```

Nach dem zweiten Chunk ruft der `HaltAfterChunkListener`
`Runtime.getRuntime().halt(1)` auf: Die JVM stirbt ohne Shutdown-Hook, wie bei
einem echten Absturz. Der Dienst ist weg, die Execution steht auf `STARTED`.

Nach einem Neustart ohne Schalter:

```bash
curl localhost:8080/jobs/running                  # die Leiche, Status STARTED
curl -X POST 'localhost:8080/jobs/order-import?lauf=1'   # 409, die Instanz gilt als aktiv
curl -X POST localhost:8080/jobs/1/recover        # Status FAILED
curl -X POST localhost:8080/jobs/1/restart        # nimmt ab dem letzten Commit auf
```

Der wiederaufgenommene `importStep` liest vier statt sechs Zeilen: Die beiden
aus dem ersten Chunk sind committet, der Reader-Zustand steht im
`ExecutionContext`. `importStepB` lief noch gar nicht und verarbeitet seine
sechs Zeilen vollständig. Danach stehen zwölf Zeilen in `book_order`, keine
doppelt.

## Szenario 3: Doppelstart

Zwei gleichzeitige Starts mit demselben Parameter:

```bash
curl -X POST 'localhost:8080/jobs/order-import?lauf=1' &
curl -X POST 'localhost:8080/jobs/order-import?lauf=1' &
```

Einer bekommt 202, der andere 409. Der Schutz sitzt im Unique Constraint von
`BATCH_JOB_INSTANCE`, nicht in einer Prüfung davor. Bei einem echten Wettlauf
kommt deshalb keine saubere Batch-Ausnahme an, sondern eine
`DuplicateKeyException`.

Zwei gleichzeitige Starts mit **verschiedenen** Parametern laufen dagegen
beide an und arbeiten auf denselben Daten. Mit `--ops.lock=true` weist die
Sperrtabelle `job_lock` den zweiten Start ab, und `GET /jobs/running` zeigt
nur einen Lauf. Der `JobLockListener` gibt die Sperre frei, sobald der Job
endet. Nach dem Absturz aus Szenario 2 bleibt sie liegen, und genau das ist
das Betriebsproblem, über das im Modul gesprochen wird.

## Szenario 4: Zwei Steps gleichzeitig

```bash
java -jar target/sb-advanced-batch-operations-demo-1.0.0-SNAPSHOT.jar                    # next
java -jar target/sb-advanced-batch-operations-demo-1.0.0-SNAPSHOT.jar --ops.parallel=true # split
```

Derselbe Job, einmal mit `next` und einmal mit `split` über einen eigenen
`TaskExecutor`. Gemessen an `spring.batch.job`: 4,99 Sekunden nacheinander,
2,50 Sekunden gleichzeitig. In beiden Fällen stehen danach zwölf Zeilen in
`book_order`, sechs je Step.

Die Zeit spart nur, wer wartet: Die Verzögerung im Processor steht für einen
langsamen Fremdaufruf. Zwei Steps, die rechnen, teilen sich dieselben Kerne.

## Szenario 5: Sehen, was passiert ist

```bash
curl localhost:8080/actuator/metrics/spring.batch.job
curl localhost:8080/actuator/metrics/spring.batch.step
```

Die Metrik trägt Job-Name und Status als Tags und die Laufzeit als Wert. Die
Metadatentabellen liefern dieselbe Information dauerhaft:

```bash
java -cp ~/.m2/repository/com/h2database/h2/*/h2-*.jar org.h2.tools.Shell \
  -url "jdbc:h2:file:./target/operations" -user "" -password "" \
  -sql "select step_name, read_count, write_count, commit_count from batch_step_execution;"
```

Das leere Benutzerfeld ist kein Versehen: Die Anwendung öffnet die Datenbank
ohne Benutzernamen, mit `-user sa` scheitert die Anmeldung. Der Dienst muss
gestoppt sein, sonst hält er die Datei gesperrt.
