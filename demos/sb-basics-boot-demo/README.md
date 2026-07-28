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

1. **Starter und AutoConfiguration:** In der `pom.xml` steht genau eine
   Web-Abhaengigkeit — `spring-boot-starter-web`. Anwendung starten und
   zeigen: Ein Tomcat laeuft auf Port 8080, ohne dass ihn jemand
   konfiguriert haette. Wer sehen will, was Spring Boot alles entschieden
   hat, startet einmal mit `--debug` und wirft einen Blick in den
   Condition-Evaluation-Report.
2. **Properties-Bindung:** `GET /info` aufrufen — die Antwort zeigt die
   Werte aus `application.properties`, gebunden an das Record
   `ShopProperties` (`shop.page-size` wird per Relaxed Binding zu
   `pageSize`).
3. **Profile:** Mit `--spring.profiles.active=dev` neu starten. Zwei
   Effekte: `/info` meldet `"pageSize":5` (die Profil-Datei gewinnt gegen
   die Basis-Datei), und im Log erscheint die Startmeldung des
   `DevDataInitializer` — die Bean existiert nur in diesem Profil. Zum
   Vergleich mit `prod` starten: `"pageSize":50`, keine Startmeldung.
4. **Rangfolge der Property-Quellen — die Pointe:** Mit
   `--shop.page-size=99` starten. `/info` meldet `"pageSize":99` —
   Kommandozeilenargumente schlagen jede Property-Datei. Dieselbe
   Anwendung, dasselbe Jar, drei Konfigurationswege mit klarer Rangfolge:
   Kommandozeile vor Profil-Datei vor Basis-Datei.

## Starten

```bash
mvn spring-boot:run
```

Oder als Fat-JAR — so laesst sich die Rangfolge der Property-Quellen am
saubersten vorfuehren, weil das Jar unveraendert bleibt:

```bash
mvn package
java -jar target/sb-basics-boot-demo-1.0.0-SNAPSHOT.jar \
     --server.port=8081 --shop.page-size=99
curl localhost:8081/info
```

Erwartete Antwort (verkuerzt): `"pageSize":99` — die Kommandozeile hat
die `20` aus der Datei ueberschrieben.

## Tests

```bash
mvn test
```

Der Test aktiviert das Profil `dev` und prueft, dass die Profil-Datei die
Basis-Properties ueberschreibt (`pageSize == 5`).
