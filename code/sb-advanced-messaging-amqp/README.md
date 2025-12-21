# AMQP-Demo (RabbitMQ)

Dieses Modul zeigt Schritt für Schritt, wie man mit Spring Boot und RabbitMQ Nachrichten austauscht.

## Was wird demonstriert?

- **Exchanges & Routing Keys**: Ein Topic-Exchange `orders.topic` verteilt Bestellungen auf unterschiedliche Queues per Routing Key (`order.standard.#`, `order.priority.#`, `order.#` für ein Audit-Tap). Ein Fanout-Exchange `notifications.fanout` broadcastet an mehrere Abonnenten (E-Mail & SMS).
- **Dead-Letter-Handling**: Beide Order-Queues besitzen eine Dead-Letter-Exchange `orders.dlx`, die Nachrichten in die DLQ `orders.dlq` umlenkt, wenn sie abgelehnt werden.
- **Auto-Ack vs. manuelles Ack**: Standard-Bestellungen laufen mit Auto-Ack. Priority-Bestellungen nutzen ein eigenes Listener-Container-Factory-Bean mit `AcknowledgeMode.MANUAL`. Bei Fehlern kann absichtlich ein NACK gesendet werden, was die Nachricht in die DLQ schiebt.
- **Publisher Confirms & Returns**: Das `RabbitTemplate` ist so konfiguriert, dass Publisher Confirms/Returns geloggt werden, wenn RabbitMQ etwas ablehnt.
- **Nachrichtenformat**: JSON-Konvertierung via `Jackson2JsonMessageConverter`, sodass einfache Java-Records automatisch serialisiert werden.

## Wo im Code passiert was?

- `src/main/java/tech/erben/springboot/amqpdemo/config/RabbitTopologyConfig.java`: Legt Exchanges, Queues, Bindings, den JSON MessageConverter, das `RabbitTemplate` und die manuell ackende Listener-Factory an.
- `src/main/java/tech/erben/springboot/amqpdemo/service/OrderMessagingService.java`: Baut Nachrichten (mit Zeitstempel und UUID) und schickt sie mit dem `RabbitTemplate` an die Exchanges.
- `src/main/java/tech/erben/springboot/amqpdemo/messaging/OrderListeners.java`: Alle `@RabbitListener`-Methoden – verarbeitet Bestellungen, schreibt Logs, schickt bei `simulateError=true` ein NACK in die DLQ.
- `src/main/java/tech/erben/springboot/amqpdemo/service/InMemoryDeliveryLog.java`: Einfaches In-Memory-Log, damit man ohne Datenbank sehen kann, welche Nachrichten wohin zugestellt wurden.
- `src/main/java/tech/erben/springboot/amqpdemo/web/MessagingDemoController.java`: HTTP-API zum Senden von Nachrichten und zum Auslesen/Löschen des Logs.
- `src/main/resources/application.yml`: RabbitMQ-Connection (localhost/guest/guest) und aktivierte Publisher Confirms/Returns.

## Voraussetzungen

- Laufender RabbitMQ auf `localhost:5672` mit `guest/guest`.
- Java 21 und Maven.

## Starten

```bash
cd sb-advanced-messaging-amqp
mvn spring-boot:run
```

Die Anwendung läuft danach auf `http://localhost:8080`.

## API – Schritt für Schritt testen

1) **Standard-Bestellung (auto-ack)**  

   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"customer":"Alice","item":"Book","quantity":2,"priority":false,"simulateError":false}'
   ```

   Erwartung: landet in `orders.standard`, wird auto-acknowledged und zusätzlich im Audit-Tap `orders.audit` verarbeitet.

2) **Priority-Bestellung mit manuellem Ack**  

   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"customer":"Bob","item":"Laptop","quantity":1,"priority":true,"simulateError":false}'
   ```

   Erwartung: Listener ackt manuell; bleibt beim Erfolg in `orders.priority` verarbeitet und ebenfalls im Audit-Tap.

3) **Priority-Bestellung absichtlich fehlschlagen lassen** (`simulateError=true`)  

   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"customer":"Eve","item":"Phone","quantity":1,"priority":true,"simulateError":true}'
   ```

   Erwartung: Listener schickt `basicNack(..., requeue=false)`, Nachricht wird über die DLX in `orders.dlq` umgeleitet.

4) **Broadcast an alle (Fanout)**  

   ```bash
   curl -X POST http://localhost:8080/api/announcements \
     -H "Content-Type: application/json" \
     -d '{"message":"System maintenance at 22:00"}'
   ```

   Erwartung: Nachricht landet gleichzeitig in `notifications.email` und `notifications.sms`.

5) **Logs ansehen** (zeigen, was wirklich zugestellt wurde)  

   ```bash
   curl http://localhost:8080/api/logs
   ```

   Optional gefiltert nach Queue: `curl "http://localhost:8080/api/logs?queue=orders.dlq"`.

6) **Logs löschen** (damit der nächste Test sauber ist)  

   ```bash
   curl -X DELETE http://localhost:8080/api/logs
   ```

## Was sieht man im Log?

Das Log enthält Zeilen wie:

- Queue-Name (z. B. `orders.priority`, `orders.dlq`, `notifications.email`)
- Eine kurze Notiz, was passiert ist (z. B. „priority order (manual-ack)“ oder „simulateError=true -> basicNack to DLQ“)
- Den Payload (OrderMessage oder BroadcastMessage)
- Zeitstempel der Verarbeitung

Damit kann man gut nachvollziehen:

- Automatisches vs. manuelles Acknowledge
- Dead-Letter-Routing bei Fehlern
- Topic-Routing (Standard vs. Priority vs. Audit-Tap)
- Fanout an mehrere Konsumenten gleichzeitig

## Warum ist das nützlich?

Die Demo ist ein kompaktes Nachschlagewerk für typische RabbitMQ-Patterns in Spring Boot:

- Wie man Exchanges, Queues und Bindings per Java Config aufsetzt
- Wie man das `RabbitTemplate` für Publisher Confirms/Returns konfiguriert
- Wie man `@RabbitListener` mit manuellem Ack nutzt
- Wie Dead-Letter-Exchanges/-Queues funktionieren und getestet werden können
- Wie man schnell per HTTP Nachrichten schickt und den Effekt live beobachtet, ohne extra UI
