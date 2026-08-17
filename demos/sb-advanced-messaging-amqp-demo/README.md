# AMQP-Demo (RabbitMQ)

Nachrichtenaustausch mit Spring Boot und RabbitMQ: Topic- und Fanout-Routing, Dead-Letter-Handling, Auto-Ack gegen manuelles Ack, Publisher Confirms.

## Topologie

| Baustein | Verhalten |
| --- | --- |
| Topic-Exchange `orders.topic` | Verteilt Bestellungen per Routing Key auf `order.standard.#`, `order.priority.#` und `order.#` (Audit-Tap) |
| Fanout-Exchange `notifications.fanout` | Broadcast an `notifications.email` und `notifications.sms` |
| Dead-Letter-Exchange `orders.dlx` | Beide Order-Queues lenken abgelehnte Nachrichten nach `orders.dlq` um |
| Acknowledge | `orders.standard` läuft mit Auto-Ack, `orders.priority` über eine eigene Listener-Container-Factory mit `AcknowledgeMode.MANUAL` |
| Publisher Confirms und Returns | Das `RabbitTemplate` loggt, was RabbitMQ ablehnt |
| Nachrichtenformat | JSON über `JacksonJsonMessageConverter`, Records werden automatisch serialisiert |

## Wo im Code

- `src/main/java/tech/erben/springboot/amqpdemo/config/RabbitTopologyConfig.java`: Exchanges, Queues, Bindings, JSON-MessageConverter, `RabbitTemplate` und die manuell ackende Listener-Factory.
- `src/main/java/tech/erben/springboot/amqpdemo/service/OrderMessagingService.java`: Baut Nachrichten mit Zeitstempel und UUID und schickt sie an die Exchanges.
- `src/main/java/tech/erben/springboot/amqpdemo/messaging/OrderListeners.java`: Alle `@RabbitListener`-Methoden; schickt bei `simulateError=true` ein NACK in die DLQ.
- `src/main/java/tech/erben/springboot/amqpdemo/service/InMemoryDeliveryLog.java`: In-Memory-Log der Zustellungen, damit die Demo ohne Datenbank auskommt.
- `src/main/java/tech/erben/springboot/amqpdemo/web/MessagingDemoController.java`: HTTP-API zum Senden von Nachrichten und zum Auslesen und Löschen des Logs.
- `src/main/resources/application.yml`: RabbitMQ-Connection (localhost/guest/guest), Publisher Confirms und Returns aktiviert.

## Voraussetzungen

- RabbitMQ auf `localhost:5672` mit `guest/guest`
- Java 21 und Maven

## Starten

```bash
cd sb-advanced-messaging-amqp-demo
mvn spring-boot:run
```

Die Anwendung läuft danach auf `http://localhost:8080`.

## API Schritt für Schritt testen

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

5) **Zustellungen ansehen**

   ```bash
   curl http://localhost:8080/api/logs
   ```

   Optional gefiltert nach Queue: `curl "http://localhost:8080/api/logs?queue=orders.dlq"`.

6) **Log löschen**, damit der nächste Test sauber ist

   ```bash
   curl -X DELETE http://localhost:8080/api/logs
   ```

## Inhalt des Logs

Jeder Eintrag enthält den Queue-Namen (z.B. `orders.priority`, `orders.dlq`, `notifications.email`), eine kurze Notiz zum Verlauf (z.B. "priority order (manual-ack)" oder "simulateError=true -> basicNack to DLQ"), den Payload (`OrderMessage` oder `BroadcastMessage`) und den Zeitstempel der Verarbeitung.
