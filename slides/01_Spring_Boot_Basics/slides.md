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
section.densest { font-size: 17.5px; }
</style>

# Spring Boot Basics

---

## In diesem Modul

* Was Spring Boot dem Spring-Container hinzufügt und was nicht
* Starter: kuratierte Dependency-Bündel
* AutoConfiguration: der Classpath entscheidet, was konfiguriert wird
* `@SpringBootApplication` und die Rolle der Paketstruktur
* Konfiguration: `application.properties`, `@ConfigurationProperties`, Profile
* Toolchains: Initializr, DevTools, `spring-boot:run` vs. Fat-JAR
* Demo: die Buchhandlung als Web-Anwendung

---

<!-- _class: denser -->
## Ohne Boot

Modul 00 hat gezeigt: Der Container verbindet die Beans. Aber wer konfiguriert alles andere?

Eine Web-Anwendung mit "purem" Spring bedeutet:

* **Dependencies von Hand wählen**: Spring MVC, Jackson, Logging, Validation etc. und alle Versionen müssen zueinander passen.
* **Infrastruktur konfigurieren**: `DispatcherServlet` registrieren, Message-Converter einrichten, Fehlerbehandlung aufsetzen.
* **Server bereitstellen**: WAR bauen, in einen extern installierten Tomcat deployen, Server separat pflegen.
* **Umgebungen unterscheiden**: Dev gegen Prod, jede Umgebung ein eigenes Konfigurations-Konstrukt.

Nichts davon ist Fachlogik und alles davon ist bei jedem Projekt gleich.

---

## Die vier "Versprechen" von Boot

Spring Boot ersetzt den Container nicht, es nimmt uns aber die Arbeit um ihn herum ab:

| Feature                            | Mittel                                                  |
|------------------------------------|---------------------------------------------------------|
| Keine Versionskonflikte            | **Starter** + gemeinsamer Versionskatalog (BOM)         |
| Keine Infrastruktur-Konfiguration  | **AutoConfiguration** anhand des Classpaths             |
| Kein externer Server               | **Eingebetteter** Tomcat, Jetty oder Undertow           |
| Einblick in die laufende Anwendung | Production-Ready-Features (**Actuator**, eigenes Modul) |

In dieser Hinsicht ist Spring Boot eine **Komfortschicht**.

---

# Starter

---

<!-- _class: dense -->
## Was ist ein Starter?

Ein Starter ist ein **Dependency-Bündel** für einen Anwendungsfall:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

* Nur **eine** Abhängigkeit. Transitiv kommen Spring MVC, Jackson und der eingebettete Tomcat hinzu.
* **Keine Versionsnummer**: die regelt der Versionskatalog von Spring Boot (gleich mehr dazu).
* Starter enthalten selbst kaum Code. Sie sind vor allem ein `pom.xml`, das die richtigen Bibliotheken in passenden Versionen bündelt.
* Sichtbar machen: `mvn dependency:tree` zeigt, was ein Starter mitbringt.

---

<!-- _class: denser -->
## Die wichtigsten Starter

| Starter                        | Bringt mit                                             |
|--------------------------------|--------------------------------------------------------|
| `spring-boot-starter`          | Container, Logging, AutoConfiguration. Basis von allem |
| `spring-boot-starter-web`      | Spring MVC, Jackson, eingebetteter Tomcat              |
| `spring-boot-starter-data-jpa` | JPA, Hibernate, Spring Data (Modul *Data*)             |
| `spring-boot-starter-security` | Authentifizierung & Autorisierung (Modul *Security*)   |
| `spring-boot-starter-actuator` | Health, Metriken, Insights (Modul *Actuator*)          |
| `spring-boot-starter-test`     | JUnit, AssertJ, Mockito, Spring-Test-Support           |

* Konvention: offizielle Starter heißen `spring-boot-starter-*`.
* Jeder Starter zieht `spring-boot-starter` transitiv mit. Die Basis ist immer dabei.

---

<!-- _class: densest -->
## spring-boot-dependencies als BOM

Es gibt zwei Wege, an Springs Versionskatalog zu kommen. Der übliche: `spring-boot-starter-parent` als Maven-Parent. Unser Kurs-Repo hat aber schon einen eigenen Parent, deshalb der zweite Weg: der BOM-Import (*Bill of Materials*). Er steht in jedem Modul-POM selbst; der Parent steuert nur die Versions-Property bei:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version> <!-- 4.0.2 -->
            <scope>import</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>
```

* `dependencyManagement` kennt ihr aus Modul 00: legt **Versionen** fest, zieht selbst nichts in den Classpath.
* Der Import holt Springs komplette Versionsliste ins Projekt, ohne die Parent-Rolle zu belegen.

---

## Versionen: was Boot für uns verwaltet

Der Katalog `spring-boot-dependencies` pinnt hunderte Bibliotheken auf getestete, zueinander passende Versionen:

* Alle Spring-Projekte (Framework, Data, Security, ...)
* Die üblichen Verdächtigen: Jackson, Hibernate, SLF4J/Logback, Tomcat, JUnit, Mockito...

Konsequenzen für unsere `pom.xml`:

* Dependencies aus dem Katalog stehen ohne `<version>` im POM.
* Ein Boot-Upgrade (eine Versionsnummer ändern) hebt den ganzen Stack konsistent an.

---

# AutoConfiguration

---

## Die Idee: Classpath entscheidet

**Convention over Configuration:** Statt dass wir Boot sagen, was wir wollen, schaut Boot nach, was da ist.

* Beim Start prüft Boot den Classpath: *Welche Bibliotheken sind vorhanden?*
* Für alles Gefundene registriert es sinnvolle Standard-Beans
* Liegt Spring MVC auf dem Classpath → Boot konfiguriert `DispatcherServlet`, Message-Converter und den eingebetteten Tomcat.
* Liegt ein JDBC-Treiber auf dem Classpath → Boot konfiguriert eine `DataSource` (Modul *Data*).

---

<!-- _class: dense -->
## Beispiel: spring-boot-starter-web

Unsere Demo hat genau eine Web-Abhängigkeit im POM: `spring-boot-starter-web`. Was Boot daraus macht:

| Boot sieht auf dem Classpath | Boot konfiguriert                                   |
|------------------------------|-----------------------------------------------------|
| Tomcat-Klassen               | Eingebetteten Tomcat auf Port `8080`                |
| Spring MVC                   | `DispatcherServlet`, Request-Mapping, Fehlerseiten  |
| Jackson                      | JSON-Serialisierung für `@RestController`-Antworten |

Ergebnis: Ein `@RestController` wie unser `ShopInfoController` funktioniert ohne eine Zeile Infrastruktur-Konfiguration

---

## Was passiert beim Start?

Konzeptionell läuft AutoConfiguration in drei Fragen ab:

1. **Classpath:** Ist die Bibliothek da? (z.B. Tomcat, Jackson, ein JDBC-Treiber)
2. **Eigene Beans:** Hat die Anwendung schon selbst eine Bean dieses Typs definiert? Dann macht Boot nichts.
3. **Properties:** Konfiguration über `application.properties`, z.B. `server.port` für den Tomcat-Port.

Die Standard-Beans entstehen also nur, wenn nichts dagegen spricht.

---

## Eigene Beans gewinnen immer

Die wichtigste Regel im Umgang mit AutoConfiguration:

* Boot konfiguriert Standard-Beans nur als Fallback
* Definieren wir selbst eine Bean desselben Typs, z.B. per `@Bean`-Methode oder Stereotyp, dann wird Boot sie nicht überschreiben

Typische Anwendungsfälle:

* Eigener `ObjectMapper` mit speziellen Jackson-Einstellungen
* Eigene `DataSource` mit besonderem Connection-Pooling

---

## Debuggen: Condition-Report

Was hat Boot eigentlich alles entschieden? Der **Condition-Evaluation-Report** zeigt es:

```bash
java -jar target/sb-basics-boot-demo-1.0.0-SNAPSHOT.jar --debug
```

Im Log erscheinen zwei Listen:

* **Positive matches**: AutoConfigurations, die aktiv wurden, samt Begründung (*"Klasse X war auf dem Classpath"*).
* **Negative matches**: was nicht aktiv wurde und warum (*"Klasse fehlt"*, *"eigene Bean vorhanden"*).

---

# @SpringBootApplication

---

<!-- _class: densest -->
## Drei Annotationen in einer

Der Einstiegspunkt unserer Demo:

```java
@SpringBootApplication
@ConfigurationPropertiesScan
public class BootDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootDemoApplication.class, args);
    }
}
```

`@SpringBootApplication` fasst drei Annotationen zusammen:

| Annotation                 | Wirkung                                        |
|----------------------------|------------------------------------------------|
| `@Configuration`           | Die Klasse darf `@Bean`-Methoden enthalten     |
| `@EnableAutoConfiguration` | Schaltet die AutoConfiguration ein             |
| `@ComponentScan`           | Scannt ab dem Package dieser Klasse (Modul 00) |

`@ConfigurationPropertiesScan` kommt in unserer Demo dazu, wir sehen später warum

---

<!-- _class: dense -->
## Warum die Paketstruktur zählt

`@ComponentScan` startet ohne weitere Angaben im **Package der Application-Klasse**:

```text
tech.erben.springboot.basics.boot
├── BootDemoApplication.java   ← Root: hier beginnt der Scan
├── ShopInfoController.java    ← wird gefunden
├── ShopProperties.java        ← wird gefunden
└── DevDataInitializer.java    ← wird gefunden
```

* Konvention: Application-Klasse ins Root-Package, alle Beans in Sub-Packages darunter.
* Klassen außerhalb dieses Baums sieht der Scan nicht. Es käme eine `NoSuchBeanDefinitionException`.
* Auch AutoConfiguration nutzt dieses Root-Package als Bezugspunkt, z.B. beim Entity-Scan (Modul *Data*).

---

# Konfiguration

---

<!-- _class: densest -->
## application.properties und application.yaml

Boot lädt Konfiguration automatisch aus `src/main/resources`, z.B.

```properties
spring.application.name=bookstore
shop.name=Buchhandlung Erben
shop.currency=EUR
shop.page-size=20
```

Dieselbe Konfiguration als `application.yaml`:

```yaml
spring:
  application:
    name: bookstore
shop:
  name: Buchhandlung Erben
  currency: EUR
  page-size: 20
```

* Beide Formate sind gleichwertig. Geschmackssache, aber bitte im Projekt einheitlich bleiben.
* Konvention für eigene Properties: gruppiert unter einem Präfix (`shop.*`), kebab-case (`page-size`).

---

<!-- _class: dense -->
## @Value vs. @ConfigurationProperties

```java
@Value("${shop.name}")
private String name;
```

Das skaliert schlecht, sobald Werte zusammengehören:

|                  | `@Value`                     | `@ConfigurationProperties`                |
|------------------|------------------------------|-------------------------------------------|
| Bindung          | Einzelwert pro Feld          | Ganze Präfix-Gruppe als Objekt            |
| Typsicherheit    | Konvertierung pro Feld       | Ein typisiertes Objekt, Fehler beim Start |
| Wiederverwendung | Platzhalter überall kopieren | Eine Bean, überall injizierbar            |
| IDE-Support      | Kaum                         | Autovervollständigung via Metadaten       |

---

<!-- _class: denser -->
## @ConfigurationProperties mit Records

Der typsichere Weg in unserer Demo: ein Record bindet alle `shop.*`-Properties:

```java
@ConfigurationProperties("shop")
public record ShopProperties(String name, String currency, int pageSize) {
}
```

* **Constructor-Binding:** Boot ruft den Record-Konstruktor mit den gebundenen Werten auf. Das Objekt ist danach unveränderlich.
* **Relaxed Binding:** `shop.page-size` aus der Properties-Datei landet im Parameter `pageSize`. kebab-case und camelCase werden automatisch abgeglichen.
* `@ConfigurationProperties` ist keine Stereotyp-Annotation. Der Component-Scan übersieht den Typ, egal ob Record oder Klasse. Deshalb steht `@ConfigurationPropertiesScan` an der `BootDemoApplication`: es registriert alle `@ConfigurationProperties`-Typen im Package-Baum.
* Verwendung wie jede Bean: `ShopInfoController` bekommt `ShopProperties` per Konstruktor-Injection.

---

## Reihenfolge: wer gewinnt?

Dieselbe Property kann aus mehreren Quellen kommen. Die für den Einstieg wichtigsten fünf in Reihenfolge der Priorität:

1. **Kommandozeile** — `--shop.page-size=99`
2. **Umgebungsvariablen** — `SHOP_PAGESIZE=99` (Relaxed Binding)
3. **Profil-Datei** — `application-dev.properties` (nur bei aktivem Profil)
4. **Basis-Datei** — `application.properties`
5. **Defaults im Code** — z.B. `@Value`-Default aus Modul 00

---

<!-- _class: dense -->
## Konfiguration von außerhalb des JARs

Zwei Properties erweitern, wo Boot nach Konfiguration sucht:

```properties
# Weitere Datei einbinden — "optional:" toleriert ihr Fehlen
spring.config.import=optional:file:./shop-lokal.properties

# Zusätzlicher Suchort neben src/main/resources
spring.config.additional-location=file:/etc/bookstore/
```

* `spring.config.import` zieht eine weitere Konfigurationsdatei hinzu, auch eine, die nicht `application.properties` heißt.
* `spring.config.additional-location` nennt zusätzliche Ablageorte, die Boot neben den Standardorten durchsuch: externe Konfiguration ohne Neubau des JARs.
* Was `spring.config.import` außer Dateien noch importieren kann, zeigt Modul *11_Configuration*.

---

<!-- _class: denser -->
## Profile

Profile schalten Konfiguration und Beans je nach Umgebung um:

```properties
# application-dev.properties — nur bei aktivem Profil "dev"
shop.page-size=5
```

```properties
# application-prod.properties — nur bei aktivem Profil "prod"
shop.page-size=50
```

Auch Beans können an ein Profil gebunden sein:

```java
@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {
    // läuft nur, wenn Profil "dev" aktiv ist
}
```

* Ohne Profil `dev` legt der Container die Bean gar nicht erst an
* Typischer Einsatz: Testdaten, Dev-Tooling, Stubs für externe Systeme

---

<!-- _class: dense -->
## Profile aktivieren

Die Namenskonvention `application-<profil>.properties` kennt Boot automatisch. Aktiviert wird per Property:

```bash
# Kommandozeile
java -jar target/sb-basics-boot-demo-1.0.0-SNAPSHOT.jar --spring.profiles.active=dev

# Umgebungsvariable
SPRING_PROFILES_ACTIVE=prod java -jar target/sb-basics-boot-demo-1.0.0-SNAPSHOT.jar
```

* Mehrere Profile gleichzeitig: `--spring.profiles.active=dev,local`. Spätere überschreiben frühere.
* Profil-Dateien ergänzen die Basis-Datei: `application.properties` gilt immer, das Profil überschreibt nur einzelne Werte (bei uns: `shop.page-size`).
* Welche Profile aktiv sind, zeigt das Startlog
* Kein Profil aktiv? Dann gilt das implizite Profil `default`.

---

# Entwicklung und Betriebsarten

---

## Spring Initializr

Neue Boot-Projekte startet man nicht von Hand, sondern generiert sie:

* **`start.spring.io`** — Web-Oberfläche: Build-Tool, Sprache, Boot-Version, Java-Version wählen
* Dieselbe Funktion steckt in den IDEs: IntelliJ (*New Project → Spring Boot*), Eclipse/STS, VS Code.
* Generiert wird ein lauffähiges Skelett: `pom.xml` mit gewählten Startern, Application-Klasse, leere `application.properties`, ein erster Test.

---

<!-- _class: denser -->
## Projektstruktur

So sieht ein Boot-Projekt aus:

```text
sb-basics-boot-demo
├── pom.xml
└── src
    ├── main
    │   ├── java/tech/erben/springboot/basics/boot
    │   │   ├── BootDemoApplication.java
    │   │   ├── ShopInfoController.java
    │   │   ├── ShopProperties.java
    │   │   └── DevDataInitializer.java
    │   └── resources
    │       ├── application.properties
    │       ├── application-dev.properties
    │       └── application-prod.properties
    └── test/java/...
```

* Maven-Standard-Layout: Code unter `src/main/java`, Konfiguration unter `src/main/resources`.
* Application-Klasse im Root-Package. Der Component-Scan findet alles darunter.

---

<!-- _class: denser -->
## DevTools

`spring-boot-devtools` beschleunigt den Entwicklungszyklus:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

* **Automatischer Restart:** Ändert sich eine Klasse im Build-Output, startet die Anwendung neu. Deutlich schneller als ein Kaltstart, weil nur der eigene Code neu geladen wird.
* **Sinnvolle Dev-Defaults:** z.B. deaktivierte Template-Caches.
* **LiveReload:** Browser-Refresh bei Ressourcen-Änderungen.
* Im **Fat-JAR ist DevTools automatisch deaktiviert**, es kann nicht versehentlich in Produktion aktiv sein.

---

## spring-boot:run vs. Fat-JAR

Zwei Arten, die Anwendung zu starten:

```bash
# Entwicklung: direkt aus Maven, ohne Packaging
mvn spring-boot:run

# Auslieferung: ein selbstständiges JAR, nur eine JVM nötig
mvn package
java -jar target/sb-basics-boot-demo-1.0.0-SNAPSHOT.jar
```

Das **Fat-JAR** (executable JAR) enthält den eigenen Code, alle Dependencies und den eingebetteten Tomcat. Deshalb genügt `java -jar`, kein externer Server.

---

# Demo

---

<!-- _class: densest -->
## Was wir gleich ansehen

`demos/sb-basics-boot-demo`: die Buchhandlung als Web-Anwendung

| Baustein              | Zeigt                                                                       |
|-----------------------|-----------------------------------------------------------------------------|
| `pom.xml`             | Ein Starter (`spring-boot-starter-web`), BOM-Import, explizites `repackage` |
| `BootDemoApplication` | `@SpringBootApplication` + `@ConfigurationPropertiesScan`                   |
| `ShopProperties`      | Record-Binding aller `shop.*`-Properties                                    |
| `ShopInfoController`  | `GET /info` zeigt wirksame Konfiguration und aktive Profile                 |
| `DevDataInitializer`  | Bean existiert nur im Profil `dev`                                          |

Ablauf: einmal `mvn package`, dann dasselbe JAR mehrfach starten: ohne Argumente (`"pageSize":20`), mit `--spring.profiles.active=dev` (`"pageSize":5`), mit `prod` (`"pageSize":50`) und zum Schluss: `--shop.page-size=99` überschreibt alle Dateien.

**Ausblick:** Modul *11_Configuration* vertieft die vollständige Property-Rangfolge, die Technik hinter AutoConfiguration und den Bau eigener Starter.
