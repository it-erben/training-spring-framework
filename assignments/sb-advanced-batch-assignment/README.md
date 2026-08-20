# Übung: Spring Batch, Teilnehmer-Import mit Fehlertoleranz

Unsere Kursverwaltung importiert nun Anmeldungen aus einer CSV-Datei in die
Tabelle `participant`. Die Datei `participants-defekt.csv` enthält acht
Anmeldungen. Dabei haben zwei Zeilen zu wenige Spalten und eine trägt eine E-Mail
ohne `@`. Eine weitere Herausforderung: Der Kurskatalog, den der Processor befragt, antwortet bei jedem
vierten Aufruf nicht.

Ihr baut den Job auf und macht ihn fehlertolerant.

## Lernziele

- Reader, Processor und Writer als Beans bauen und zu einem Step verdrahten
- Items im Processor umformen und mit `null` verwerfen
- Einen Step mit `faultTolerant()` gegen Datenfehler und technische Ausfälle
  absichern
- Übersprungene Zeilen über einen `SkipListener` in eine Ausschussdatei
  schreiben

## Voraussetzungen

- Java 21, Maven 3.9
- Inhalte aus Modul *18 Batch* (Folien und Demo
  `demos/sb-advanced-batch-demo`)

## Was liegt bereit?

Alle Klassen liegen unter `tech.erben.springboot.batch.task`:

| Klasse                                       | Zustand                                                              |
|----------------------------------------------|----------------------------------------------------------------------|
| `ParticipantImportApplication`               | Fertig: Einstiegspunkt                                               |
| `ParticipantLine`                            | Fertig: Record für eine Zeile der CSV-Datei                          |
| `CourseCatalogClient`                        | Fertig: wirft `CatalogUnavailableException` bei jedem vierten Aufruf |
| `CatalogUnavailableException`                | Fertig: technischer Fehler, den der Retry auffängt                   |
| `RejectListener`                             | Fertig: `SkipListener`, schreibt Ausschüsse in eine Datei            |
| `ParticipantJobConfig`                       | **Hier müsst ihr arbeiten**: fünf Beans mit sieben TODO-Marken       |
| `ParticipantBeansTest`                       | Drei Tests ohne Spring-Kontext für die Aufgaben 1 bis 3              |
| `ParticipantJobTest`                         | Ein Test mit Spring-Kontext für die Aufgaben 4 bis 7                 |
| `src/main/resources/schema.sql`              | Legt die Tabelle `participant` an                                    |
| `src/main/resources/participants-defekt.csv` | Acht Zeilen, zwei davon mit zu wenigen Spalten                       |

Der Startzustand ist **absichtlich defekt**. Der Aufruf

```bash
mvn test -DskipAssignmentTests=false
```

meldet vier Fehlschläge, einen je Aufgabenblock. Die ersten drei nennen die
Aufgabe direkt (`UnsupportedOperationException: Aufgabe 1`), der vierte
scheitert daran, dass der Anwendungskontext ohne fertige Beans nicht startet.

## Vorgehen

Als Vorlage könnt ihr folgenden Job aus den Demos benutzen:

```
demos/sb-advanced-batch-demo/sb-advanced-batch-demo-finished/
  src/main/java/tech/erben/springboot/batch/OrderImportJobConfig.java
```

Wenn ihr euch dieses Datei zum Vorbild nehmt, müsst ihr folgendes umbenennen:

| Demo (Buchhandlung)   | Übung (Kursverwaltung)  |
|-----------------------|-------------------------|
| `OrderLine`           | `ParticipantLine`       |
| `orderReader`         | `participantReader`     |
| `pricingProcessor`    | `participantProcessor`  |
| `orderWriter`         | `participantWriter`     |
| `importStep`          | `participantImportStep` |
| `orderImportJob`      | `participantImportJob`  |
| `PricingClient`       | `CourseCatalogClient`   |
| Tabelle `book_order`  | Tabelle `participant`   |

Arbeitet von oben nach unten und lasst nach jeder Aufgabe die Tests laufen:

```bash
mvn test -DskipAssignmentTests=false
```

Es gibt zwei Testklassen:

- `ParticipantBeansTest` holt die Beans direkt aus der Konfigurationsklasse,
  ohne Spring hochzufahren. Die Aufgaben 1, 2 und 3 werden deshalb **einzeln**
  grün, sobald ihr sie erledigt habt.
- `ParticipantJobTest` startet den echten Job und braucht dafür alle fünf
  Beans. Er schlägt bis zur Aufgabe 5 mit "Failed to load ApplicationContext"
  fehl. Das ist so erwartet und keine Fehlersuche eurerseits wert.

## Aufgaben

### 1. `participantReader`

**Ziel:** Eine Bean, die `participants-defekt.csv` Zeile für Zeile als
`ParticipantLine` liefert.

**Vorlage:** `orderReader` in der Demo.

Auf dem `FlatFileItemReaderBuilder<ParticipantLine>` braucht ihr diese
Aufrufe, in dieser Reihenfolge, und am Ende `.build()`:

| Aufruf              | Wert                                               |
|---------------------|----------------------------------------------------|
| `.name(...)`        | `"participantReader"`                              |
| `.resource(...)`    | `new ClassPathResource("participants-defekt.csv")` |
| `.linesToSkip(...)` | `1`, die Kopfzeile ist keine Anmeldung             |
| `.delimited()`      | ohne Argument, schaltet auf Trennzeichen-Format    |
| `.names(...)`       | die vier Spaltennamen in der Reihenfolge der Datei |
| `.targetType(...)`  | `ParticipantLine.class`                            |

**Stolperstein:** Die Namen bei `.names(...)` müssen exakt den Komponenten des
Records entsprechen, sonst findet Spring die Zuordnung nicht.

**Test:** `aufgabe1_readerLiestDieErsteAnmeldung`

### 2. `participantProcessor`

**Ziel:** Eine Bean, die eine Zeile aufräumt, ungültige Zeilen wegwirft und
den Kurskatalog befragt.

**Vorlage:** `pricingProcessor` in der Demo. Dort steht dasselbe Muster als
Lambda in einer Zeile.

Ein `ItemProcessor<ParticipantLine, ParticipantLine>` ist ein Lambda, das ein
`ParticipantLine` bekommt und eines zurückgibt. Drei Schritte:

1. E-Mail normalisieren: `line.email().trim().toLowerCase()`.
2. Enthält sie **kein** `@`: `null` zurückgeben. Das Item ist damit verworfen,
   erreicht den Writer nie und zählt als `filterCount`.
3. Sonst `courseCatalogClient.check(line.courseCode())` aufrufen und ein
   **neues** `ParticipantLine` mit der normalisierten E-Mail zurückgeben.

**Stolperstein:** `ParticipantLine` ist ein Record und damit unveränderlich.
Ihr könnt die E-Mail nicht nachträglich setzen sondern baut ein neues Objekt:
`new ParticipantLine(line.participantId(), line.fullName(), email, line.courseCode())`.

**Test:** `aufgabe2_processorNormalisiertEmailUndVerwirftUngueltige`

### 3. `participantWriter`

**Ziel:** Eine Bean, die einen ganzen Chunk in die Tabelle `participant`
schreibt.

**Vorlage:** `orderWriter` in der Demo.

Auf dem `JdbcBatchItemWriterBuilder<ParticipantLine>`:

| Aufruf             | Wert                                                                                                                              |
|--------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| `.dataSource(...)` | den Parameter `dataSource` durchreichen                                                                                           |
| `.sql(...)`        | `insert into participant (participant_id, full_name, email, course_code) values (:participantId, :fullName, :email, :courseCode)` |
| `.beanMapped()`    | ohne Argument                                                                                                                     |

**Stolperstein:** Links vom `values` stehen die **Spalten** der Tabelle
(`participant_id`), rechts die **Record-Komponenten** mit Doppelpunkt
(`:participantId`). `beanMapped()` stellt die Verbindung her.

**Test:** `aufgabe3_writerSchreibtNachParticipant`

### 4. `participantImportStep`

**Ziel:** Reader, Processor und Writer zu einem Step verbinden.

**Vorlage:** `importStep` in der Demo.

```java
return new StepBuilder("participantImportStep", jobRepository)
        .<ParticipantLine, ParticipantLine>chunk(3)
        .transactionManager(transactionManager)
        .reader(participantReader)
        .processor(participantProcessor)
        .writer(participantWriter)
        .build();
```

**Stolperstein:** Die Generics vor `chunk(3)` sind notwendig.
Sie legen Ein- und Ausgabetyp des Steps fest; ohne sie kompiliert die Kette
nicht.

### 5. `participantImportJob`

**Ziel:** Den Step zu einem Job machen.

**Vorlage:** `orderImportJob` in der Demo, dort mit drei Steps. Hier genügt
einer.

```java
return new JobBuilder("participantImportJob", jobRepository)
        .start(participantImportStep)
        .build();
```

### 6. Fehlertoleranz im Step

**Ziel:** Der Job soll nicht mehr an der ersten kaputten Zeile abbrechen.

**Vorlage:** `importStep` in der Demo, der Teil ab `.faultTolerant()`.

Ergänzt in Aufgabe 4 **vor** dem abschließenden `.build()`:

| Aufruf                                      | Bedeutung                                           |
|---------------------------------------------|-----------------------------------------------------|
| `.faultTolerant()`                          | schaltet Skip und Retry überhaupt erst ein          |
| `.skip(FlatFileParseException.class)`       | kaputte CSV-Zeilen überspringen                     |
| `.skipLimit(3)`                             | höchstens drei übersprungene Zeilen, danach Abbruch |
| `.retry(CatalogUnavailableException.class)` | Katalogausfall wiederholen                          |
| `.retryLimit(3)`                            | höchstens drei Versuche je Item                     |

**Warum zwei verschiedene Mechanismen?** Eine kaputte CSV-Zeile bleibt kaputt,
egal wie oft man sie liest. Wir wollen sie immer überspringen. Der Kurskatalog dagegen
antwortet beim nächsten Versuch vielleicht. Daher hilft ein Retry.

**Stolperstein:** `.faultTolerant()` muss **vor** den `skip`- und
`retry`-Aufrufen stehen; es liefert erst den Builder, der sie kennt.

### 7. Ausschussdatei

**Ziel:** Übersprungene Zeilen sollen nachvollziehbar sein, nicht still
verschwinden.

**Vorlage:** `importStep` in der Demo, der `.skipListener(...)`-Aufruf.

Hängt an dieselbe Kette:

```java
.skipListener(new RejectListener(Path.of("target/rejected-participants.csv")))
```

`RejectListener` liegt fertig im Projekt, ihr müsst ihn nur erzeugen und
registrieren.

**Test:** Prüft Aufgaben 4 bis 7 gemeinsam
`aufgabe4Bis7_jobLaeuftFehlertolerantDurch`. Er wird erst grün, wenn alle vier
fertig sind. Ohne Fehlertoleranz bricht der Lauf an der ersten kaputten Zeile ab.

Bei korrekter Lösung endet der Job `COMPLETED`: zwei Zeilen werden beim Lesen
übersprungen (`skipCount = 2`), eine wird vom Processor verworfen
(`filterCount = 1`), fünf Anmeldungen werden geschrieben (`writeCount = 5`),
und `target/rejected-participants.csv` enthält die beiden übersprungenen
Zeilen. Der Katalogausfall trifft einen gültigen Datensatz und wird durch den
Retry beim nächsten Versuch aufgefangen, er führt zu keinem Skip.

## Lösung

Siehe `solutions/sb-advanced-batch-solution`.
