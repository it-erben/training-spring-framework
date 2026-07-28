# Demo: Spring Boot Basics — Starter, AutoConfiguration, Properties und Profile

Diese Demo zeigt, was Spring Boot auf den Spring-Container obendrauf legt,
wieder am Beispiel der Buchhandlung: Ein einziger Starter bringt den
kompletten Web-Stack mit, AutoConfiguration konfiguriert ihn ohne eine
Zeile eigenen Codes, `@ConfigurationProperties` bindet Konfiguration
typsicher an ein Record, und Profile schalten Werte und Beans je nach
Umgebung um.

Anders als bei der Core-Demo gibt es keine Start-Variante — in diesem
Modul wird konfiguriert, nicht programmiert. Das Projekt ist fertig und
dient als Spielwiese fuer Properties, Profile und Kommandozeilenargumente.

## Bausteine

| Klasse | Zweck |
| --- | --- |
| `BootDemoApplication` | `@SpringBootApplication` plus `@ConfigurationPropertiesScan` |
| `ShopProperties` | Record, bindet alle `shop.*`-Properties per Constructor-Binding |
| `ShopInfoController` | `GET /info` zeigt die wirksame Konfiguration und die aktiven Profile |
| `DevDataInitializer` | Existiert nur im Profil `dev` und loggt beim Start |

Die Property-Dateien bauen aufeinander auf:

| Datei | Inhalt |
| --- | --- |
| `application.properties` | Basis: `shop.page-size=20` |
| `application-dev.properties` | Profil `dev`: `shop.page-size=5` |
| `application-prod.properties` | Profil `prod`: `shop.page-size=50` |

## Ablauf der Live-Demo

Alle Schritte nutzen dasselbe Fat-JAR — einmal bauen, danach ist jeder
Neustart nur ein neuer `java -jar`-Aufruf mit anderen Argumenten:

```bash
mvn package
```

1. **Starter und AutoConfiguration:** Ohne Argumente starten:

   ```bash
   java -jar target/sb-basics-boot-demo-1.0.0-SNAPSHOT.jar
   ```

   In der `pom.xml` steht genau eine Web-Abhaengigkeit —
   `spring-boot-starter-web`. Trotzdem laeuft jetzt ein Tomcat auf Port
   8080, ohne dass ihn jemand konfiguriert haette. Wer sehen will, was
   Spring Boot alles entschieden hat, startet einmal mit `--debug` und
   wirft einen Blick in den Condition-Evaluation-Report.

2. **Properties-Bindung:** `curl localhost:8080/info` — die Antwort zeigt
   die Werte aus `application.properties`, gebunden an das Record
   `ShopProperties` (`shop.page-size` wird per Relaxed Binding zu
   `pageSize`): `"pageSize":20`, `"activeProfiles":[]`.

3. **Profil `dev`:** Mit aktivem Profil `dev` neu starten:

   ```bash
   java -jar target/sb-basics-boot-demo-1.0.0-SNAPSHOT.jar \
        --spring.profiles.active=dev
   ```

   Zwei Effekte: `/info` meldet `"pageSize":5` und
   `"activeProfiles":["dev"]` — die Profil-Datei gewinnt gegen die
   Basis-Datei. Und im Log erscheint die Startmeldung des
   `DevDataInitializer`, denn die Bean existiert nur in diesem Profil.

4. **Profil `prod`:** Derselbe Vergleich mit `prod`:

   ```bash
   java -jar target/sb-basics-boot-demo-1.0.0-SNAPSHOT.jar \
        --spring.profiles.active=prod
   ```

   `/info` meldet `"pageSize":50` und `"activeProfiles":["prod"]`. Die
   Startmeldung bleibt aus — den `DevDataInitializer` legt der Container
   in diesem Profil gar nicht erst an.

5. **Rangfolge der Property-Quellen — die Pointe:** Profil-Datei und
   Kommandozeile gleichzeitig:

   ```bash
   java -jar target/sb-basics-boot-demo-1.0.0-SNAPSHOT.jar \
        --spring.profiles.active=dev --shop.page-size=99
   ```

   `/info` meldet `"pageSize":99` — das Kommandozeilenargument schlaegt
   die Profil-Datei (5) und die Basis-Datei (20). Dieselbe Anwendung,
   dasselbe Jar, drei Konfigurationswege mit klarer Rangfolge:
   Kommandozeile vor Profil-Datei vor Basis-Datei.

## Starten ohne Fat-JAR

Alternativ laeuft die Demo auch direkt aus Maven:

```bash
mvn spring-boot:run
```

Laeuft parallel schon etwas auf Port 8080, hilft `--server.port=8081` —
auch der Port ist nur eine Property.

## Tests

```bash
mvn test
```

Jede Behauptung der Live-Demo ist durch einen Test abgesichert:

| Test | Behauptung |
| --- | --- |
| `ShopInfoControllerTest` | Der Web-Stack startet per AutoConfiguration; `GET /info` liefert die gebundenen Basis-Properties und die aktiven Profile |
| `ShopPropertiesTest` | Profil `dev` ueberschreibt `shop.page-size` auf 5 |
| `ShopPropertiesProdTest` | Profil `prod` ueberschreibt `shop.page-size` auf 50 |
| `DevDataInitializerProfileTest` | Die `dev`-Bean existiert nur bei aktivem Profil `dev` |
| `CommandLinePrecedenceTest` | Kommandozeilenargumente schlagen Profil- und Basis-Datei |
