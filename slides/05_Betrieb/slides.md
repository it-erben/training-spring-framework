---
marp: true
theme: default
header: Spring Boot Basics
footer: Alexander Erben
paginate: true
---
<!-- Dichte-Stufen gegen Folienueberlauf, siehe tools/check-slide-overflow.mjs -->
<style>
section.dense { font-size: 24.5px; }
section.denser { font-size: 21px; }
</style>

# Betrieb: Von der IDE in den Container

---

## In diesem Modul

* Logging mit SLF4J und Logback
* Actuator: Health, Info, Metriken
* Konfiguration: Environment-Variablen, die Namensregel, Kommandozeile
* Fat-JAR und ein Dockerfile

---

# Logging

---

<!-- _class: dense -->
## SLF4J und Logback

`System.out.println` ist in Produktion nicht sinnvoll. Es enthält kein Level, kein Zeitstempel und ist nicht filterbar. Stattdessen loggt jede Spring-Boot-Anwendung über zwei Bausteine:

* **SLF4J** ist die **Fassade** und damit die API, gegen die euer Code spricht (`Logger`, `log.info(...)`). Auch Spring selbst und praktisch jede Java-Bibliothek loggen dagegen.
* **Logback** ist die **Implementierung** dahinter. Sie formatiert und schreibt die Zeilen tatsächlich.
* Beides steckt in jedem Starter (`spring-boot-starter-logging` kommt transitiv mit) — deshalb loggt jede Boot-Anwendung vom ersten Start an formatiert und gefiltert, ganz ohne Konfiguration.

---

<!-- _class: dense -->
## Einen Logger holen

Das Muster ist immer gleich, hier aus dem `OrderController` der Demo:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger log = LoggerFactory.getLogger(OrderController.class);

log.info("Bestellübersicht angefragt");
log.debug("Liefere {} Bestellungen aus", orders.size());
```

* Die Klasse als Argument wird zum **Logger-Namen** (`tech.erben.springboot.basics.operations.OrderController`. Darüber wird später gefiltert.
* Die `{}` sind **Platzhalter**, die String-Konkatenation vermeiden. Der String wird nur gebaut, wenn das Level das Ausgeben erlaubt.

---

<!-- _class: dense -->
## Die Log-Level

Jede Log-Zeile hat ein Level, jeder Logger eine Schwelle. Ausgegeben wird nur, was **auf oder über** der Schwelle liegt:

| Level   | Wofür                                                 |
|---------|-------------------------------------------------------|
| `ERROR` | Etwas ist defekt und braucht sofortige Aufmerksamkeit |
| `WARN`  | Noch nicht defekt, aber verdächtig                    |
| `INFO`  | Die wichtigen Ereignisse des normalen Betriebs        |
| `DEBUG` | Details für die Fehlersuche                           |
| `TRACE` | Extrem feinkörnig und selten gebraucht                |

Steht die Schwelle auf `INFO`, sind `debug`- und `trace`-Zeilen unsichtbar. Typisch in Produktion: `INFO` für die eigene Anwendung, `WARN` für Fremdbibliotheken.

---

## Level konfigurieren

Die Schwelle wird pro Logger-Name gesetzt. Logger-Namen sind Package-Pfade, die Konfiguration wirkt also **hierarchisch**. Aus der `application.properties` der Demo:

```properties
logging.level.tech.erben=INFO
```

* Gilt für `tech.erben` und alles darunter.
* Es ist eine ganz normale Property. Sie ließe sich also auch von außen setzen.

---

# Actuator

---

## Was macht der Actuator?

Sobald eine Anwendung betrieben wird, stellen andere Fragen an sie: Lebt sie noch? Welche Version läuft? Wie voll ist der Heap? Der **Actuator** beantwortet diese Fragen über HTTP:

| Endpoint            | Antwort                                |
|---------------------|----------------------------------------|
| `/actuator/health`  | Ist die Anwendung gesund?              |
| `/actuator/info`    | Metadaten: wer bin ich?                |
| `/actuator/metrics` | Messwerte: Speicher, Requests, Threads |
| `/actuator/loggers` | Log-Level anzeigen **und ändern**      |

---

## Einbinden und freischalten

Aktiviert wird der Actuator mit einer Dependency und vier Zeilen Konfiguration:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```properties
management.endpoints.web.exposure.include=health,info,metrics,loggers
```

* Standardmäßig ist über HTTP **nur `health`** erreichbar — alles Weitere muss man bewusst freischalten.
* Und das aus gutem Grund: `loggers` erlaubt Schreibzugriff, `metrics` verrät Interna.

---

## /actuator/health

```json
{ "status": "UP",
  "components": {
    "diskSpace": { "status": "UP", "details": { "free": ... } }, ... } }
```

* Der Gesamtstatus **aggregiert** einzelne Indikatoren. Boot bringt sie passend zum Classpath mit: `diskSpace` immer, eine Datenbank-Prüfung, sobald eine DataSource da ist.
* Die Details zeigt die Demo nur wegen `management.endpoint.health.show-details=always`. Ohne diese Zeile antwortet der Endpoint schlicht `{"status":"UP"}`.
* Meldet ein einziger Indikator `DOWN`, ist Gesamtstatus ebenfalls `DOWN`

---

## /actuator/info und /actuator/metrics

`/actuator/info` liefert statische Metadaten:

```properties
management.info.env.enabled=true
info.app.name=Buchhandlung GFU
```

`/actuator/metrics` listet die verfügbaren Messwerte (u.a. `jvm.memory.used`); Mit Name im Pfad (`/actuator/metrics/jvm.memory.used`) wir der aktuelle Wert geliefert.

* JVM, Tomcat und HTTP-Requests werden automatisch gemessen. Eigene Metriken kommen im Advanced-Teil.

---

<!-- _class: dense -->
## Log-Level zur Laufzeit ändern

Ausgangslage: `tech.erben` steht auf `INFO`, `curl localhost:8080/api/orders` zeigt die `info`-Zeile, die `debug`-Zeilen des `OrderService` fehlen. Dann:

```bash
curl -X POST localhost:8080/actuator/loggers/tech.erben \
     -H "Content-Type: application/json" \
     -d '{"configuredLevel":"DEBUG"}'
```

* Antwort **204**. Der nächste Request auf `/api/orders` schreibt die `debug`-Zeilen in die Konsole **ohne Neustart und ohne Deployment**.
* Das braucht man, wenn in Produktion ein Fehler auftritt, der sich lokal nicht reproduzieren lässt und nicht in den Logs erscheint wegen Log-Level-Konfiguration
* `GET /actuator/loggers/tech.erben` zeigt jederzeit das konfigurierte und das wirksame Level.

---

## Ausblick: eigene Health Indicators

Die mitgelieferten Indikatoren prüfen Infrastruktur. Ob euer **fachlich** kritisches System erreichbar ist (etwa der Zahlungsdienstleister der Buchhandlung), weiß Boot nicht.

Dafür gibt es das Interface `HealthIndicator`: eine eigene Bean, eine `health()`-Methode. Der Check erscheint als weiterer Eintrag unter `components` und fließt in den Gesamtstatus ein.

---

# Konfiguration von außen

---

## Warum nichts ins Image gehört

Wir bevorzugen **ein Artefakt für alle Umgebungen**:

* Dasselbe Image durchläuft Dev, Test und Produktion. Was sich je Umgebung unterscheidet (URLs, Zugangsdaten, Log-Level), darf **nicht einbacken** sein.
* Secrets im Image sind ein Sicherheits-Incident: Jeder mit Zugriff auf die Registry kann sie auslesen.
* Ein Wert von außen erfordert keinen neuen Build: Konfiguration ändern, Container neu starten, fertig.

---

## Environment-Variablen

Der Standardweg im Container: Docker reicht Variablen mit `-e` hinein:

```bash
docker run --rm -p 8080:8080 \
    -e LOGGING_LEVEL_TECH_ERBEN=DEBUG \
    sb-basics-operations-demo
```

* Boot liest beim Start die Umgebung und mappt die Variablen auf Properties: `LOGGING_LEVEL_TECH_ERBEN` überschreibt das `logging.level.tech.erben=INFO` aus dem JAR.
* Wie wird aus einem Property-Namen der Variablenname? Dafür gibt es eine feste Regel.

---

## Namensregel

Aus dem Property-Namen wird in **drei Schritten** der Variablenname. Am Beispiel `shop.page-size` aus Modul 01:

1. Punkte durch Unterstriche ersetzen: `shop_page-size`
2. Bindestriche **ersatzlos streichen**: `shop_pagesize`
3. Alles groß: `SHOP_PAGESIZE`

---

## Kommandozeilenargumente

Die zweite Quelle von außen: `--` plus exakter Property-Name

```bash
java -jar app.jar --logging.level.tech.erben=DEBUG --server.port=9090
```

* Schlägt alles andere, auch Environment-Variablen.
* Merksatz für den Betrieb: **Defaults ins JAR, Umgebungsspezifisches per Environment, Kommandozeile zum Debuggen.**

---

# Container

---

## Das Fat-JAR

`mvn package` baut in der Demo ein ausführbares Fat-JAR: eigene Klassen plus sämtliche Dependencies plus eingebetteter Tomcat. `java -jar` genügt dann zum Start

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <version>${spring-boot.version}</version>
    <executions>
        <execution><goals><goal>repackage</goal></goals></execution>
    </executions>
</plugin>
```

---

## Ein einfaches Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/sb-basics-operations-demo-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
mvn package
docker build -t sb-basics-operations-demo .
docker run --rm -p 8080:8080 sb-basics-operations-demo
```

Basis-Image ist eine schlanke **JRE** ohne JDK und kein Maven

---

## spring-boot:build-image

Es geht auch ganz ohne Dockerfile: das Boot-Plugin baut selbst ein Image:

```bash
mvn spring-boot:build-image
```

Nutzt **Cloud Native Buildpacks**: Das Plugin analysiert die Anwendung und schichtet ein Image nach Best Practices

---

<!-- _class: denser -->
## Ausblick auf den Advanced-Teil

| Thema                    | Was ihr dort lernt                                                                                                   |
|--------------------------|----------------------------------------------------------------------------------------------------------------------|
| Eigene Metriken & Health | Micrometer-Counter und -Timer, eigene `HealthIndicator` und Actuator-Endpoints, Prometheus & Grafana                 |
| Distributed Tracing      | Micrometer Tracing, Context Propagation über HTTP und Messaging, Observation API                                     |
| Security                 | Spring Security: Filterkette, Method Security, OAuth2, CORS/CSRF, mTLS                                               |
| Messaging                | Kafka, JMS und AMQP, Spring Cloud Stream, Schema Registry, Dead-Letter Queues, Saga Pattern und Transactional Outbox |
| Resilience-Patterns      | Circuit Breaker und Bulkhead gegen Kaskadenfehler in verteilten Systemen                                             |
| Microservice-Architektur | DDD und Bounded Contexts, API Gateway, Service Discovery                                                             |
