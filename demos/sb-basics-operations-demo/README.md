# Demo: Betrieb — Actuator, Log-Level zur Laufzeit und Docker

Diese Demo zeigt, was Spring Boot für den Betrieb mitbringt:
Health-Checks, Metadaten, Metriken und steuerbares Logging, alles ohne
eine Zeile eigenen Betriebscode. Die Anwendung selbst ist bewusst klein:
ein `OrderController` mit einer Bestellübersicht und ein `OrderService`,
der auf `debug` loggt. Diese `debug`-Zeilen sind der Gegenstand von
Schritt 3.

Anders als bei den bisherigen Modulen gibt es kein start/finished-Paar —
das Modul ist klein genug, um es am Stück zu zeigen.

## Die Endpoints

| Endpoint | Verhalten |
| --- | --- |
| `GET /api/orders` | 200 mit drei Beispielbestellungen als JSON; loggt auf `info` und `debug` |
| `GET /actuator/health` | 200 mit `{"status":"UP"}` und Details der einzelnen Indikatoren (z.B. `diskSpace`) |
| `GET /actuator/info` | Statische Metadaten, u.a. der App-Name `Buchhandlung Erben` |
| `GET /actuator/metrics` | Liste der verfügbaren Metriken, u.a. `jvm.memory.used` |
| `GET /actuator/loggers/tech.erben` | Aktuelles und wirksames Log-Level des Packages |
| `POST /actuator/loggers/tech.erben` | Stellt das Log-Level zur Laufzeit um — 204, kein Neustart |

Standardmäßig ist nur `/actuator/health` über HTTP erreichbar. Die
übrigen Endpoints schaltet `application.properties` gezielt frei
(`management.endpoints.web.exposure.include`) — in Produktion gilt: so
wenig wie möglich exponieren oder den Management-Port absichern.
Kleiner Stolperstein bei `/actuator/info`: Der Contributor, der
`info.*`-Properties durchreicht, ist seit Spring Boot 2.6 abgeschaltet
und braucht `management.info.env.enabled=true`.

## Ablauf der Live-Demo

1. **Starten und umsehen:** `mvn spring-boot:run`, dann
   `/actuator/health`, `/actuator/info` und `/actuator/metrics` im
   Browser oder per `curl` durchgehen. Nichts davon wurde programmiert —
   das ist der Actuator-Starter plus vier Zeilen Konfiguration.
2. **Der Ausgangszustand:** `curl localhost:8080/api/orders` — in der
   Konsole erscheint die `info`-Zeile des Controllers, aber keine der
   `debug`-Zeilen aus dem `OrderService`. `logging.level.tech.erben=INFO`
   filtert sie weg.
3. **Log-Level zur Laufzeit umstellen:**

   ```bash
   curl -X POST localhost:8080/actuator/loggers/tech.erben \
        -H "Content-Type: application/json" \
        -d '{"configuredLevel":"DEBUG"}'
   curl localhost:8080/api/orders
   ```

   Jetzt stehen die `debug`-Zeilen in der Konsole, ohne Neustart und
   ohne Deployment — der Weg an einen Fehler heran, der sich nur in
   Produktion zeigt.
4. **Ins Image packen:** `mvn package` baut dank explizit gebundenem
   `repackage`-Goal ein ausführbares Fat-JAR (siehe `pom.xml` — ohne
   `spring-boot-starter-parent` passiert das nicht automatisch). Das
   Dockerfile kopiert es in ein schlankes JRE-Image:

   ```bash
   mvn package
   docker build -t sb-basics-operations-demo .
   docker run --rm -p 8080:8080 sb-basics-operations-demo
   ```

   Derselbe Health-Endpoint dient in Kubernetes als Liveness- und
   Readiness-Probe.

## Starten und Testen

```bash
mvn spring-boot:run    # oder: mvn package && java -jar target/sb-basics-operations-demo-1.0.0-SNAPSHOT.jar
mvn test               # 5 Tests
```

Jede Verhaltensbehauptung ist durch einen Test abgesichert:

| Behauptung | Test |
| --- | --- |
| `GET /api/orders` liefert 200 und drei Beispielbestellungen als JSON | `OrderApiTest.ordersReturnsThreeSampleOrders` |
| `/actuator/health` antwortet mit 200, `{"status":"UP"}` und Details (`diskSpace`) | `ActuatorEndpointsTest.healthReturnsUpWithDetails` |
| `/actuator/info` enthält den App-Namen `Buchhandlung Erben` | `ActuatorEndpointsTest.infoContainsAppName` |
| `/actuator/metrics` ist freigeschaltet und kennt `jvm.memory.used` | `ActuatorEndpointsTest.metricsListsJvmMetrics` |
| `POST /actuator/loggers/tech.erben` antwortet mit 204 und stellt das Level ohne Neustart um | `LogLevelSwitchTest.debugLinesAppearOnlyAfterRuntimeSwitch` |
| Die `debug`-Zeilen erscheinen erst **nach** dem Umschalten, vorher nicht | `LogLevelSwitchTest.debugLinesAppearOnlyAfterRuntimeSwitch` |

## Anschluss

Letztes Modul des Basis-Teils. Der Advanced-Teil setzt hier an: eigene
Metriken und Tracing mit Micrometer, Security mit Spring Security und
OAuth2, Messaging mit Kafka und AMQP, Resilience-Muster wie Circuit
Breaker und Retry.
