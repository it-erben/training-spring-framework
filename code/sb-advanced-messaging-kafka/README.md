# Kafka-Demo

Dieses Modul zeigt, wie man mit Spring Boot und Apache Kafka Nachrichten produziert, konsumiert und Fehlerszenarien über ein Dead-Letter-Topic behandelt.

## Was wird demonstriert?

- **Topics & Partition Keys**: Bestellungen landen abhängig vom Flag `priority` in `orders.standard` oder `orders.priority` (jeweils 3 Partitionen). Der `customer` ist der Partition-Key, damit eine Kundin ihre Reihenfolge behält.
- **Audit-Topic**: Jede Bestellung wird zusätzlich auf `orders.audit` gespiegelt.
- **Consumer Groups als Fanout**: Das Broadcast-Topic `notifications.broadcast` wird von zwei Consumer-Gruppen (`notifications-email`, `notifications-sms`) parallel gelesen – eine Nachricht, zwei unabhängige Abnehmer.
- **Manuelles Ack vs. Container-Ack**: Standard-Bestellungen nutzen containerseitiges Batch-Acking, Priority-Bestellungen werden manuell ge-acknowledged.
- **Dead-Letter-Topic**: Bei `simulateError=true` wirft der Priority-Listener eine Exception. Der `DefaultErrorHandler` schickt die Nachricht nach einem Retry partitionserhaltend auf `orders.dlt`.
- **JSON-Serialisierung mit Type-Headern**: Producer/Consumer nutzen `JsonSerializer`/`JsonDeserializer`, damit Records automatisch gemarshalt werden.

## Wo im Code passiert was?

- `src/main/java/tech/erben/springboot/kafkademo/config/KafkaTopologyConfig.java`: Legt Topics an, Listener-Factories (Batch vs. manuelles Ack) und den DeadLetterPublishingRecoverer fest.
- `src/main/java/tech/erben/springboot/kafkademo/service/OrderMessagingService.java`: Baut Nachrichten und sendet sie an das Ziel-Topic sowie an das Audit-Topic.
- `src/main/java/tech/erben/springboot/kafkademo/messaging/OrderListeners.java`: @KafkaListener für Standard/ Priority/ Audit/ Dead-Letter und zwei Broadcast-Consumer-Gruppen.
- `src/main/java/tech/erben/springboot/kafkademo/service/InMemoryDeliveryLog.java`: Einfaches In-Memory-Log mit Filter nach Topic.
- `src/main/java/tech/erben/springboot/kafkademo/web/MessagingDemoController.java`: HTTP-API zum Senden von Nachrichten und zum Lesen/Löschen des Logs.
- `src/main/resources/application.yml`: Kafka-Bootstrap (localhost:9092), JSON-Serializer/Deserializer, auto-create Topics per Admin-Client.

## Voraussetzungen

- Laufender Kafka-Broker auf `localhost:9092` (KRaft oder ZooKeeper; Topic-Erzeugung über den Admin-Client ist aktiv).
- Java 21 und Maven.

## Starten

```bash
cd sb-advanced-messaging-kafka
mvn spring-boot:run
```

Die Anwendung läuft danach auf `http://localhost:8080`.

## API – Schritt für Schritt testen

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

5) **Logs ansehen**  
   ```bash
   curl http://localhost:8080/api/logs
   ```
   Optional gefiltert nach Topic:  
   ```bash
   curl "http://localhost:8080/api/logs?topic=orders.dlt"
   ```

6) **Logs löschen**  
   ```bash
   curl -X DELETE http://localhost:8080/api/logs
   ```

Damit sieht man:
- Wie Producer/Consumer mit JSON-Records funktionieren
- Unterschied zwischen containerseitigem und manuellem Ack
- Fanout über Consumer-Gruppen
- Dead-Letter-Handling mit `DeadLetterPublishingRecoverer` und `DefaultErrorHandler`
