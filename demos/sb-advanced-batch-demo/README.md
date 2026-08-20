# Batch-Demo (Spring Batch)

Ein Job, der Bestellungen aus einer CSV-Datei in die Tabelle `book_order`
importiert und anschließend einen Umsatzbericht schreibt. Der Import-Step ist
fehlertolerant: defekte Zeilen werden übersprungen, ein ausgefallener
Preisdienst wird wiederholt.

`sb-advanced-batch-demo-start` ist der Ausgangspunkt für das Live-Coding,
`sb-advanced-batch-demo-finished` die vollständige Referenz mit Tests.

## Aufbau

| Step | Ablauf |
| --- | --- |
| `importStep` | `FlatFileItemReader` → `pricingProcessor` → `JdbcBatchItemWriter` nach `book_order`, Chunk-Größe 2 |
| `reportStep` | `JdbcCursorItemReader` über `book_order` → `FlatFileItemWriter` nach `target/revenue-report.csv`, Chunk-Größe 10 |
| `archiveStep` | Tasklet: verschiebt den Bericht nach `target/archive/`, ohne Reader und Writer |

Tabelle `book_order`: `order_id varchar(32) primary key`, `title varchar(255)`,
`quantity int`, `unit_price_cents int`.

## Konfiguration

| Property | Default | Wirkung |
| --- | --- | --- |
| `demo.batch.input` | `orders.csv` | eingelesene Datei; `orders-defekt.csv` enthält zwei nicht parsebare Zeilen |
| `demo.batch.report` | `target/revenue-report.csv` | Ziel des Berichts und Quelle des Archiv-Schritts |
| `demo.batch.archive-dir` | `target/archive` | Zielverzeichnis des Archiv-Schritts |
| `demo.batch.skip-limit` | `3` | Obergrenze übersprungener Zeilen, darüber bricht der Step ab |
| `demo.batch.pricing.flaky` | `false` | schaltet den simulierten Ausfall des Preisdienstes zu |

Die Datenquelle ist dateibasiertes H2 (`./target/orders`). Das
`JobRepository` überlebt damit einen Neustart. Ein zweiter Lauf mit
identischen `JobParameters` trifft auf dieselbe `JobInstance` und scheitert
mit `JobInstanceAlreadyCompleteException`. Zum Zurücksetzen
`target/orders.mv.db` löschen.

Das Modul ist ein Batch-Artefakt: Es startet, verarbeitet und endet. Der Job
läuft beim Hochfahren (`spring.batch.job.enabled` steht per Default auf
`true`), und `main` beendet den Prozess mit dem `BatchStatus` als Exit-Code.
Ein abgeschlossener Lauf liefert `0`, ein fehlgeschlagener `5`.

## Starten

```bash
mvn -q -DskipTests package
java -jar sb-advanced-batch-demo-finished/target/sb-advanced-batch-demo-finished-1.0.0-SNAPSHOT.jar
echo $?
```

Mit defekter Eingabe und ausgefallenem Preisdienst:

```bash
java -jar sb-advanced-batch-demo-finished/target/sb-advanced-batch-demo-finished-1.0.0-SNAPSHOT.jar \
    --demo.batch.input=orders-defekt.csv \
    --demo.batch.pricing.flaky=true
```

Danach stehen die übersprungenen Zeilen in `target/rejected.csv`.

## Live-Coding

Ausgangspunkt ist `sb-advanced-batch-demo-start`. Dort sind alle Klassen außer
`OrderImportJobConfig` fertig; die fünf Beans der Konfiguration tragen
`TODO`-Marken. Jeder Zwischenstand ist lauffähig.

### Runde 1: der Job läuft

Arbeitet die Schritte 1 bis 5 in `OrderImportJobConfig` ab: Reader, Processor,
Writer, Step, Job. Danach starten:

```bash
mvn -q -DskipTests package
java -jar target/sb-advanced-batch-demo-start-1.0.0-SNAPSHOT.jar
```

Erwartet: Job `COMPLETED`, fünf Zeilen in `book_order`. `O-3` fehlt, weil der
Processor sie wegen `quantity = 0` verworfen hat, sie zählt als `filterCount`.

Startet denselben Befehl ein zweites Mal, ohne `target/orders.mv.db` zu
löschen. Der Lauf scheitert mit `JobInstanceAlreadyCompleteException`: Das
`JobRepository` erkennt dieselbe `JobInstance` wieder.

### Runde 2: zwei weitere Steps

Ergänzt `reportReader`, `reportWriter` und `reportStep` und hängt den Step mit
`.next(reportStep)` an den Job. Nach dem Lauf steht der Bericht in
`target/revenue-report.csv`.

Hängt danach `archiveStep` an. Der Step verwendet `.tasklet(...)` statt
`.chunk(...)` und den bereitliegenden `ArchiveReportTasklet`, einen einmaligen
Handgriff ohne Items. Nach dem Lauf liegt der Bericht in `target/archive/`
und nicht mehr an seinem ursprünglichen Ort.

### Runde 3: wenn es schiefgeht

Startet mit der defekten Datei und ausgefallenem Preisdienst:

```bash
java -jar target/sb-advanced-batch-demo-start-1.0.0-SNAPSHOT.jar \
    --demo.batch.input=orders-defekt.csv \
    --demo.batch.pricing.flaky=true
```

Der Job endet `FAILED`, weil die erste nicht parsebare Zeile den Step abbricht.
Ergänzt anschließend im `importStep` die Fehlertoleranz: `faultTolerant()`,
`skip(FlatFileParseException.class)` mit `skipLimit`, den
`RejectFileSkipListener`, `retry(PricingUnavailableException.class)` und
`retryLimit(3)`. Derselbe Befehl endet danach `COMPLETED`, zwei Zeilen stehen
in `target/rejected.csv`.

## Tests

`sb-advanced-batch-demo-finished` bringt zwei Integrationstests mit:
`OrderImportJobTest` prüft den fehlertoleranten Normalfall
(`skipCount = 2`, `filterCount = 1`, `writeCount = 3`) und dass der
Archiv-Schritt den Bericht verschoben hat,
`SkipLimitExceededTest` den Abbruch bei `demo.batch.skip-limit=1`
(`skipCount = 1`, `rollbackCount = 1`, Status `FAILED`).

```bash
mvn -q test
```
