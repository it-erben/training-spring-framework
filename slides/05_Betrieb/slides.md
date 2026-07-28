---
marp: true
theme: default
header: Spring Boot Basics
footer: Alexander Erben
paginate: true
---

# Betrieb: Von der IDE in den Container

---

## In diesem Modul

* Logging mit SLF4J und Logback: Logger holen, Level verstehen und konfigurieren
* Actuator: Health, Info, Metriken — und Log-Level **zur Laufzeit** umstellen
* Konfiguration von außen: Environment-Variablen, die Namensregel, Kommandozeile
* Das Fat-JAR, ein Dockerfile — und zum Abschluss: was der Advanced-Teil darauf aufsetzt

Das kürzeste Modul des Kurses — und der Abschluss des Basis-Teils. Die Buchhandlung kann laufen; heute lernt sie, **betrieben** zu werden. Kurs-Stand: **Spring Boot 4.0.2, Java 21**.

---

# Logging

---

## SLF4J und Logback

`System.out.println` hat im Betrieb nichts verloren: kein Level, kein Zeitstempel, nicht filterbar. Stattdessen loggt jede Spring-Boot-Anwendung über zwei Bausteine:

* **SLF4J** ist die **Fassade** — die API, gegen die euer Code spricht (`Logger`, `log.info(...)`). Auch Spring selbst und praktisch jede Java-Bibliothek loggen dagegen.
* **Logback** ist die **Implementierung** dahinter — sie formatiert und schreibt die Zeilen tatsächlich.
* Beides steckt in jedem Starter (`spring-boot-starter-logging` kommt transitiv mit) — deshalb loggt jede Boot-Anwendung vom ersten Start an formatiert und gefiltert, ganz ohne Konfiguration.

---

## Einen Logger holen

Das immergleiche Muster — hier aus dem `OrderController` der Demo:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger log = LoggerFactory.getLogger(OrderController.class);

log.info("Bestelluebersicht angefragt");
log.debug("Liefere {} Bestellungen aus", orders.size());
```

* Die Klasse als Argument wird zum **Logger-Namen** (`tech.erben.springboot.basics.operations.OrderController`) — darüber wird später gefiltert.
* Die `{}`-**Platzhalter** statt String-Konkatenation: Der String wird nur gebaut, wenn das Level das Ausgeben erlaubt — kein Aufwand für Zeilen, die ohnehin verworfen werden.

---

## Die Log-Level

Jede Log-Zeile hat ein Level, jeder Logger eine Schwelle — ausgegeben wird, was **auf oder über** der Schwelle liegt:

| Level | Wofür |
| --- | --- |
| `ERROR` | Etwas ist kaputt und braucht Aufmerksamkeit |
| `WARN` | Noch nicht kaputt, aber verdächtig |
| `INFO` | Die wichtigen Ereignisse des normalen Betriebs |
| `DEBUG` | Details für die Fehlersuche |
| `TRACE` | Extrem feinkörnig — selten gebraucht |

Steht die Schwelle auf `INFO`, sind `debug`- und `trace`-Zeilen unsichtbar — genau der Ausgangszustand der Demo. Typisch in Produktion: `INFO` für die eigene Anwendung, `WARN` für gesprächige Fremdbibliotheken.

---

## Level konfigurieren

Die Schwelle wird pro Logger-Name gesetzt — und Logger-Namen sind Package-Pfade, die Konfiguration wirkt also **hierarchisch**. Aus der `application.properties` der Demo:

```properties
logging.level.tech.erben=INFO
```

* Gilt für `tech.erben` und alles darunter — auch für `OrderService` und `OrderController`. Beliebig granular: `logging.level.org.hibernate=WARN` daneben — laut die eigene Anwendung, leise das Framework.
* Es ist eine ganz normale Property — sie ließe sich also auch von außen setzen (gleich mehr). Aber für ein laufendes System gibt es einen besseren Weg: den Actuator.

---

## Was gehört ins Log — und was nicht

Logs liest im Ernstfall jemand um drei Uhr nachts — und im Zweifel auch ein Auditor:

* **Hinein gehört:** was passiert ist, mit welchen Kennungen (Bestell-Id, ISBN) — genug Kontext, um den Fall wiederzufinden.
* **Nicht hinein gehören:** Passwörter, Tokens, Session-Ids — und personenbezogene Daten nur, wo es wirklich sein muss (DSGVO!).
* Exceptions **mitloggen statt verschlucken**: `log.error("Bestellung fehlgeschlagen", e)` — der Stacktrace ist die halbe Diagnose. Und Level ehrlich wählen: Wer Routinefälle auf `ERROR` loggt, trainiert allen ab, `ERROR` ernst zu nehmen.

---

# Actuator

---

## Was Actuator liefert

Sobald eine Anwendung betrieben wird, stellen andere Fragen an sie: Lebt sie? Welche Version läuft? Wie voll ist der Heap? Der **Actuator** beantwortet das über HTTP — ohne eine Zeile eigenen Code:

| Endpoint | Antwort |
| --- | --- |
| `/actuator/health` | Ist die Anwendung gesund? |
| `/actuator/info` | Metadaten — wer bin ich? |
| `/actuator/metrics` | Messwerte: Speicher, Requests, Threads |
| `/actuator/loggers` | Log-Level anzeigen **und ändern** |

* Genau die Schnittstellen, die Monitoring, Load Balancer und Kubernetes erwarten — `/actuator/health` ist dort die Liveness- bzw. Readiness-Probe.

---

## Einbinden und freischalten

Eine Dependency, vier Zeilen Konfiguration — die komplette Betriebsschnittstelle der Demo:

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
* Und das aus gutem Grund: `loggers` erlaubt Schreibzugriff, `metrics` verrät Interna. Die Demo exponiert **mehr, als produktiv vertretbar wäre** — im echten Betrieb gilt: so wenig wie möglich freischalten oder den Management-Port absichern.

---

## /actuator/health

```json
{ "status": "UP",
  "components": {
    "diskSpace": { "status": "UP", "details": { "free": ... } }, ... } }
```

* Der Gesamtstatus **aggregiert** einzelne Indikatoren — Boot bringt sie passend zum Classpath mit: `diskSpace` immer, eine Datenbank-Prüfung, sobald eine DataSource da ist.
* Die Details zeigt die Demo nur wegen `management.endpoint.health.show-details=always` — ohne diese Zeile antwortet der Endpoint schlicht `{"status":"UP"}`.
* Meldet ein einziger Indikator `DOWN`, kippt der Gesamtstatus — und der Endpoint antwortet mit **503** statt 200. Genau darauf reagieren Load Balancer und Kubernetes.

---

## /actuator/info und /actuator/metrics

`/actuator/info` liefert statische Metadaten — die Demo füllt sie aus Properties:

```properties
management.info.env.enabled=true
info.app.name=Buchhandlung Erben
```

* Stolperstein: Der Contributor, der `info.*`-Properties durchreicht, ist seit Boot 2.6 **abgeschaltet** — ohne die erste Zeile bleibt der Endpoint leer.

`/actuator/metrics` listet die verfügbaren Messwerte (u.a. `jvm.memory.used`); ein Name als Pfad — `/actuator/metrics/jvm.memory.used` — liefert den aktuellen Wert.

* JVM, Tomcat und HTTP-Requests werden automatisch vermessen — eigene Metriken kommen im Advanced-Teil.

---

## Log-Level zur Laufzeit ändern

Die Pointe der Demo. Ausgangslage: `tech.erben` steht auf `INFO`, `curl localhost:8080/api/orders` zeigt die `info`-Zeile — die `debug`-Zeilen des `OrderService` fehlen. Dann:

```bash
curl -X POST localhost:8080/actuator/loggers/tech.erben \
     -H "Content-Type: application/json" \
     -d '{"configuredLevel":"DEBUG"}'
```

* Antwort **204**, und der nächste Request auf `/api/orders` schreibt die `debug`-Zeilen in die Konsole — **ohne Neustart, ohne Deployment**.
* Genau das braucht man, wenn in Produktion ein Fehler auftritt, der sich lokal nicht reproduzieren lässt: Level hoch, Fall beobachten, Level wieder runter.
* `GET /actuator/loggers/tech.erben` zeigt jederzeit das konfigurierte und das wirksame Level.

---

## Ausblick: eigene Health Indicators

Die mitgelieferten Indikatoren prüfen Infrastruktur — ob euer **fachlich** kritisches System erreichbar ist (etwa der Zahlungsdienstleister der Buchhandlung), weiß Boot nicht.

* Dafür gibt es das Interface `HealthIndicator`: eine eigene Bean, eine `health()`-Methode — und der Check erscheint als weiterer Eintrag unter `components` und fließt in den Gesamtstatus ein. Mehr als diesen Satz braucht es heute nicht: Eigene Indicators, eigene Metriken mit Micrometer und eigene Actuator-Endpoints behandelt ausführlich **Modul 15 des Advanced-Teils**.

---

# Konfiguration von außen

---

## Warum nichts ins Image gehört

Modul 01 hat das Prinzip eingeführt: **ein Artefakt für alle Umgebungen**. Im Container wird daraus eine harte Regel:

* Dasselbe Image durchläuft Dev, Test und Produktion — was sich je Umgebung unterscheidet (URLs, Zugangsdaten, Log-Level), darf **nicht einbacken** sein.
* Secrets im Image sind ein Sicherheitsvorfall: Jeder mit Zugriff auf die Registry kann sie auslesen — `docker history` vergisst nichts.
* Ein Wert von außen erfordert keinen neuen Build: Konfiguration ändern, Container neu starten, fertig.
* Die Mechanik kennt ihr schon — die Property-Reihenfolge aus Modul 01. Von außen heißt konkret: **Environment-Variablen** und **Kommandozeilenargumente**. Beide schlagen jede Datei im JAR.

---

## Environment-Variablen

Der Standardweg im Container — Docker reicht Variablen mit `-e` hinein:

```bash
docker run --rm -p 8080:8080 \
    -e LOGGING_LEVEL_TECH_ERBEN=DEBUG \
    sb-basics-operations-demo
```

* Boot liest beim Start die Umgebung und mappt die Variablen auf Properties — `LOGGING_LEVEL_TECH_ERBEN` überschreibt das `logging.level.tech.erben=INFO` aus dem JAR. Kein Docker-Spezifikum: Dasselbe funktioniert in der Shell, in systemd-Units und in Kubernetes-Manifesten.
* Nur: Wie wird aus einem Property-Namen der Variablenname? Dafür gibt es eine feste Regel — und an ihr geht in der Praxis am häufigsten etwas schief.

---

## Die Namensregel: SHOP_PAGESIZE

Aus dem Property-Namen wird in **drei Schritten** der Variablenname — am Beispiel `shop.page-size` aus Modul 01:

1. Punkte durch Unterstriche ersetzen: `shop_page-size`
2. Bindestriche **ersatzlos streichen**: `shop_pagesize`
3. Alles groß: `SHOP_PAGESIZE`

* Das ist die **dokumentierte kanonische Form** — die einzige Schreibweise, auf die man sich überall verlassen kann. Das Relaxed Binding verzeiht zwar einiges (auch `SHOP_PAGE_SIZE` mit Bindestrich → Unterstrich kommt an) — aber gewöhnt euch die kanonische Form an, statt auszuprobieren, was gerade noch durchgeht.
* Tückisch ist, was **danebenliegt**: Ein Vertipper wie `SHOP_PAGESIZ` bindet an gar nichts — kein Fehler, keine Warnung, die Anwendung startet stillschweigend mit dem Default aus dem JAR. Deshalb bei „Property wirkt nicht"-Symptomen im Container zuerst die Variablennamen prüfen.

---

## Kommandozeilenargumente

Die zweite Quelle von außen — `--` plus exakter Property-Name, bekannt aus Modul 01:

```bash
java -jar app.jar --logging.level.tech.erben=DEBUG --server.port=9090
```

* Schlägt **alles** — auch Environment-Variablen. Deshalb das Mittel der Wahl zum schnellen lokalen Ausprobieren. Im Container eher die Ausnahme: Argumente stehen sichtbar im Startbefehl; Orchestrierer wie Kubernetes arbeiten mit Environment-Variablen und gemounteten Dateien.
* Merksatz für den Betrieb: **Defaults ins JAR, Umgebungsspezifisches per Environment, Kommandozeile zum Debuggen.**

---

# Container

---

## Das Fat-JAR

`mvn package` baut in der Demo ein **ausführbares Fat-JAR**: eigene Klassen plus sämtliche Dependencies plus eingebetteter Tomcat — `java -jar` genügt, kein Application Server.

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

* Das erledigt das `repackage`-Goal des `spring-boot-maven-plugin` — **Achtung:** Nur mit `spring-boot-starter-parent` ist es automatisch gebunden. Die Demo hat einen eigenen Parent, deshalb steht die `<execution>` explizit im POM. Ohne sie entsteht ein normales, **nicht startbares** JAR — auffallen tut das erst beim `java -jar`.

---

## Ein einfaches Dockerfile

Das komplette Dockerfile der Demo — vier Anweisungen:

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

* Basis-Image ist eine schlanke **JRE** — kein JDK, kein Maven: Gebaut wird vorher, das Image führt nur aus.
* Danach ist alles von heute im Container erreichbar: `/api/orders`, `/actuator/health` — Letzteres in Kubernetes die Liveness-/Readiness-Probe.

---

## spring-boot:build-image

Es geht auch ganz ohne Dockerfile — das Boot-Plugin baut selbst ein Image:

```bash
mvn spring-boot:build-image
```

* Nutzt **Cloud Native Buildpacks**: Das Plugin analysiert die Anwendung und schichtet ein Image nach Best Practices — passende JRE, Speicher-Tuning, Dependencies in eigenen Layern für besseres Cache-Verhalten. Kein Dockerfile zu schreiben und zu pflegen — dafür weniger direkte Kontrolle und ein Docker-Daemon als Voraussetzung.
* Die Demo bleibt beim Dockerfile: Vier sichtbare Zeilen erklären mehr als eine Blackbox. Für den Einstieg gilt — Dockerfile zum Verstehen, `build-image` als komfortable Alternative im Alltag.

---

## Was jetzt fehlt — Ausblick auf den Advanced-Teil

Die Buchhandlung läuft im Container, meldet ihren Zustand und lässt sich von außen steuern. Damit ist der Basis-Teil komplett. Was der **Advanced-Teil** darauf aufbaut:

| Thema | Was ihr dort lernt |
| --- | --- |
| Eigene Metriken & Health | Micrometer-Counter und -Timer, eigene `HealthIndicator` und Actuator-Endpoints, Prometheus & Grafana |
| Distributed Tracing | Micrometer Tracing, Context Propagation über HTTP und Messaging, Observation API |
| Security | Spring Security: Filterkette, Method Security, OAuth2, CORS/CSRF, mTLS |
| Messaging | Kafka, JMS und AMQP, Spring Cloud Stream, Schema Registry, Dead-Letter Queues, Saga Pattern und Transactional Outbox |
| Resilience-Patterns | Circuit Breaker und Bulkhead gegen Kaskadenfehler in verteilten Systemen |
| Microservice-Architektur | DDD und Bounded Contexts, API Gateway, Service Discovery |

Dazu vertiefen eigene Module Konfiguration (eigene Starter, Kubernetes ConfigMaps & Secrets), Testing (Testcontainers, Contract Testing), Persistenz (Transaktionen, Migrations) und Web (HTTP-Clients, OpenAPI, Virtual Threads).

**Danke — und bis zum Advanced-Teil!**
