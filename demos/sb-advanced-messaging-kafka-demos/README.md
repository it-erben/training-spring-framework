# Kafka-Demo

Nachrichten mit Spring Boot und Apache Kafka produzieren, konsumieren und Fehler über ein Dead-Letter-Topic behandeln.

## Topologie

| Baustein | Verhalten |
| --- | --- |
| Topics und Partition Keys | Bestellungen landen je nach Flag `priority` in `orders.standard` oder `orders.priority` (je 3 Partitionen). Partition-Key ist `customer`, damit die Reihenfolge je Kundin erhalten bleibt |
| Audit-Topic | Jede Bestellung wird zusätzlich auf `orders.audit` gespiegelt |
| Consumer Groups als Fanout | `notifications.broadcast` wird von den Gruppen `notifications-email` und `notifications-sms` parallel gelesen: eine Nachricht, zwei unabhängige Abnehmer |
| Acknowledge | Standard-Bestellungen nutzen containerseitiges Batch-Acking, Priority-Bestellungen werden manuell ge-acknowledged |
| Dead-Letter-Topic | Bei `simulateError=true` wirft der Priority-Listener eine Exception. Der `DefaultErrorHandler` schickt die Nachricht nach einem Retry partitionserhaltend auf `orders.dlt` |
| Nachrichtenformat | `JsonSerializer` und `JsonDeserializer` mit Type-Headern, Records werden automatisch gemarshalt |

## Wo im Code

- `src/main/java/tech/erben/springboot/kafkademo/config/KafkaTopologyConfig.java`: Topics, Listener-Factories (Batch gegen manuelles Ack) und der `DeadLetterPublishingRecoverer`.
- `src/main/java/tech/erben/springboot/kafkademo/service/OrderMessagingService.java`: Baut Nachrichten und sendet sie an das Ziel-Topic sowie an das Audit-Topic.
- `src/main/java/tech/erben/springboot/kafkademo/messaging/OrderListeners.java`: `@KafkaListener` für Standard, Priority, Audit und Dead-Letter sowie zwei Broadcast-Consumer-Gruppen.
- `src/main/java/tech/erben/springboot/kafkademo/service/InMemoryDeliveryLog.java`: In-Memory-Log der Zustellungen mit Filter nach Topic.
- `src/main/java/tech/erben/springboot/kafkademo/web/MessagingDemoController.java`: HTTP-API zum Senden von Nachrichten und zum Lesen und Löschen des Logs.
- `src/main/resources/application.yml`: Kafka-Bootstrap (localhost:9092), JSON-Serializer und -Deserializer, Topic-Erzeugung per Admin-Client.

## Voraussetzungen

- Kafka-Broker auf `localhost:9092` (KRaft oder ZooKeeper; Topic-Erzeugung über den Admin-Client ist aktiv)
- Java 21 und Maven

## Starten

```bash
cd sb-advanced-messaging-kafka
mvn spring-boot:run
```

Die Anwendung läuft danach auf `http://localhost:8080`.

## API Schritt für Schritt testen

1) **Standard-Bestellung (Batch-Ack über Container)**

   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"customer":"Alice","item":"Book","quantity":2,"priority":false,"simulateError":false}'
   ```

   Erwartung: landet auf `orders.standard` und zusätzlich im Audit-Topic.

2) **Priority-Bestellung mit manuellem Ack**

   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"customer":"Bob","item":"Laptop","quantity":1,"priority":true,"simulateError":false}'
   ```

   Erwartung: Listener acked manuell, Audit bekommt eine Kopie.

3) **Priority-Bestellung absichtlich fehlschlagen lassen** (`simulateError=true`)

   ```bash
   curl -X POST http://localhost:8080/api/orders \
     -H "Content-Type: application/json" \
     -d '{"customer":"Eve","item":"Phone","quantity":1,"priority":true,"simulateError":true}'
   ```

   Erwartung: Listener wirft eine Exception, der `DefaultErrorHandler` published nach einem Retry nach `orders.dlt` (Partition bleibt gleich).

4) **Broadcast an mehrere Consumer-Gruppen**

   ```bash
   curl -X POST http://localhost:8080/api/announcements \
     -H "Content-Type: application/json" \
     -d '{"message":"System maintenance at 22:00"}'
   ```

   Erwartung: Nachricht wird von `notifications-email` und `notifications-sms` konsumiert.

5) **Zustellungen ansehen**

   ```bash
   curl http://localhost:8080/api/logs
   ```

   Optional gefiltert nach Topic:

   ```bash
   curl "http://localhost:8080/api/logs?topic=orders.dlt"
   ```

6) **Log löschen**

   ```bash
   curl -X DELETE http://localhost:8080/api/logs
   ```
