# Spring-Boot-Basis-Teil — Implementierungsplan

> **For agentic workers:** REQUIRED SUB-SKILL: Use
> superpowers:subagent-driven-development (recommended) or
> superpowers:executing-plans to implement this plan task-by-task. Steps use
> checkbox (`- [ ]`) syntax for tracking.

**Goal:** Dem bestehenden Spring-Boot-Advanced-Kurs einen zweitägigen
Basis-Teil aus sechs Modulen voranstellen — Slides, Demos, Übungen und
Lösungen — und Repository sowie Maven-Koordinaten passend umbenennen.

**Architecture:** Ein Repository, zwei Blöcke. Die Slide-Verzeichnisse
`00_`–`05_` bilden den Basis-Teil, die bestehenden acht Decks wandern auf
`10_`–`17_`. Jedes Basis-Modul bekommt ein eigenständiges Maven-Demo-Modul;
vier der sechs Module zusätzlich ein Assignment mit zugehöriger Solution. Die
Demos teilen sich die Fachlichkeit Buchhandlung, die Übungen die Fachlichkeit
Kursverwaltung — technisch bleibt jedes Modul unabhängig und für sich
lauffähig.

**Tech Stack:** Java 21, Spring Boot 4.0.2 (per BOM-Import, nicht
`spring-boot-starter-parent`), Maven 3.9, JUnit 5, Mockito, H2, Marp,
markdownlint-cli2.

**Spec:** `docs/superpowers/specs/2026-07-28-spring-boot-basis-teil-design.md`

---

## Global Constraints

Diese Vorgaben gelten für **jede** Task. Sie werden in den Einzeltasks nicht
wiederholt.

- **Sprache:** Alle teilnehmersichtbaren Inhalte sind deutsch — Slides,
  READMEs, Code-Kommentare, Commit-Messages. Bezeichner im Code sind
  englisch (`Book`, `findByTitle`), wie im Bestand.
- **Java:** 21. `maven.compiler.source` und `target` kommen vom Root-POM.
- **Spring Boot:** `4.0.2`, referenziert als `${spring-boot.version}`. Jedes
  ausführbare Modul importiert `spring-boot-dependencies` als BOM im
  `<dependencyManagement>`. **Kein `spring-boot-starter-parent`** — der
  Bestand nutzt durchgängig den BOM-Import, weil das Root-POM bereits Parent
  ist.
- **Maven-Koordinaten:** `groupId` `tech.erben`, `version`
  `1.0.0-SNAPSHOT`. Neue Module tragen das Präfix `sb-basics-`.
- **Package-Basis:** `tech.erben.springboot.basics.<modul>`, z. B.
  `tech.erben.springboot.basics.web`. Für Assignments und Solutions gilt
  dasselbe Package — die Solution ist eine Kopie des Assignments mit
  ausgefülltem Code.
- **Kein Lombok in Basis-Modulen.** Der Advanced-Teil nutzt es teilweise,
  aber für Teilnehmer ohne Spring-Erfahrung ist Annotation-Processing eine
  zusätzliche Fehlerquelle. Stattdessen: Java-Records für DTOs, explizite
  Getter/Setter für JPA-Entities.
- **Spring Boot 4 statt 3:** `@MockBean` und `@SpyBean` existieren nicht
  mehr. Verwende `@MockitoBean` und `@MockitoSpyBean` aus
  `org.springframework.test.context.bean.override.mockito`.
- **Fachlichkeit:** Demos = Buchhandlung (`Book`, `Author`, `Order`).
  Assignments und Solutions = Kursverwaltung (`Course`, `Participant`,
  `Trainer`). Diese Trennung ist Absicht — sie verhindert, dass die
  Demo-Lösung in die Übung kopiert wird.
- **Marp-Frontmatter** für Basis-Decks, exakt so:

  ```yaml
  ---
  marp: true
  theme: default
  header: Spring Boot Basics
  footer: Alexander Erben
  paginate: true
  ---
  ```

- **Slide-Konventionen:** Deck beginnt mit `# <Modultitel>`, dann `---`, dann
  `## In diesem Modul` mit Bullet-Überblick. Abschnitte sind `#`,
  Einzelslides `##`. Slides sind durch `---` auf eigener Zeile getrennt.
  Umfang pro Deck 400–600 Zeilen.
- **Markdown-Lint:** `npx markdownlint-cli2 "<datei>"` muss ohne Fehler
  durchlaufen. `MD013` (Zeilenlänge) ist in `.markdownlint.json`
  ausgeschaltet, aber `MD060` ist aktiv: Tabellen-Trennzeilen müssen
  `| --- | --- |` lauten, nicht `|---|---|`.
- **Assignment-README:** folgt `../../../ASSIGNMENT_README_TEMPLATE.md`
  (absolut: `/Users/aerben/repositories/it-erben/gfu/ASSIGNMENT_README_TEMPLATE.md`)
  mit den Abschnitten Titel, Kontext, Lernziele, Voraussetzungen, Was liegt
  bereit?, Aufgaben, Bonusaufgabe, Erfolgskriterien, Lösung.
- **Assignment-Vertrag:** Ein Assignment enthält Tests, die **vor** der
  Bearbeitung rot sind und nach vollständiger Bearbeitung grün. Die
  zugehörige Solution enthält dieselben Tests und ist grün. Ausnahme ist
  Task 15 (Testing-Assignment) — dort ist der Produktivcode fertig und die
  Teilnehmer schreiben die Tests.
- **Assignments müssen kompilieren.** Der Surefire-Skip hält rote Tests aus
  dem Gesamtbuild heraus, aber gegen einen Compile-Fehler hilft er nicht —
  der bricht den Reaktor für alle folgenden Module. Alle Typen und
  Methodensignaturen werden daher fertig ausgeliefert; die Aufgabe besteht
  aus Annotationen, Query-Definitionen und Methodenkörpern, nie aus „lege
  diesen Typ erst an". Der rote Ausgangszustand entsteht zur Laufzeit
  (Kontext startet nicht, Endpunkt liefert 404), nicht beim Übersetzen.
- **Gleiche Namen, verschiedene Formen:** `Course` ist in Task 4 ein Record
  (Modul 00 kennt noch keine Persistenz) und in Task 12 eine JPA-Entity mit
  Gettern und Settern. Das ist Absicht und kein Widerspruch — die Module
  sind eigenständige Maven-Artefakte mit eigenen Packages.
- **Commits:** Conventional Commits, deutsch, ein Commit pro Task-Abschluss.
  Beispiel: `feat(basics): Modul 00 Spring Core — Slides`.

---

## File Structure

### Slides (nach der Umbenennung in Task 1)

| Pfad | Verantwortung |
| --- | --- |
| `slides/00_Spring_Core/slides.md` | IoC, DI, Beans, Scopes, Maven-Grundlagen |
| `slides/01_Spring_Boot_Basics/slides.md` | Starter, Autoconfiguration, Properties, Profile |
| `slides/02_Web_REST/slides.md` | `@RestController`, Mappings, Validierung, Fehlerbehandlung |
| `slides/03_Data_JPA/slides.md` | Entities, Repositories, Queries, `@Transactional` |
| `slides/04_Testing/slides.md` | JUnit 5, Mockito, `@SpringBootTest`, `@WebMvcTest` |
| `slides/05_Betrieb/slides.md` | Logging, Actuator, externe Konfiguration, Docker |
| `slides/10_…` bis `slides/17_…` | unverändert, nur umbenannt |

### Maven-Module

| Pfad | Verantwortung |
| --- | --- |
| `demos/sb-basics-core-demo/` | Aggregator, `packaging: pom` |
| `demos/sb-basics-core-demo/sb-basics-core-demo-start/` | Ausgangszustand ohne Annotationen |
| `demos/sb-basics-core-demo/sb-basics-core-demo-finished/` | Zielzustand |
| `demos/sb-basics-boot-demo/` | einzelnes Modul, kein Aggregator |
| `demos/sb-basics-web-demo/{-start,-finished}/` | Aggregator + zwei Module |
| `demos/sb-basics-data-jpa-demo/{-start,-finished}/` | Aggregator + zwei Module |
| `demos/sb-basics-testing-demo/{-start,-finished}/` | Aggregator + zwei Module |
| `demos/sb-basics-operations-demo/` | einzelnes Modul |
| `assignments/sb-basics-core-assignment/` | Übung Modul 00 |
| `assignments/sb-basics-web-assignment/` | Übung Modul 02 |
| `assignments/sb-basics-data-jpa-assignment/` | Übung Modul 03 |
| `assignments/sb-basics-testing-assignment/` | Übung Modul 04 |
| `solutions/sb-basics-*-solution/` | je eine Lösung pro Assignment |

### Geänderte Bestandsdateien

| Pfad | Änderung |
| --- | --- |
| `pom.xml` | `artifactId` → `sb-training` |
| `demos/pom.xml` | `artifactId` → `sb-training-demos`, Parent, 6 neue Module |
| `assignments/pom.xml` | `artifactId` → `sb-training-assignments`, Parent, 4 neue Module |
| `solutions/pom.xml` | `artifactId` → `sb-training-solutions`, Parent, 4 neue Module |
| 20 bestehende Modul-POMs | nur `<parent><artifactId>` angepasst |
| `slides/template.html` | Titel und `<h1>` |
| `README.md` | neu anzulegen (Task 18) |

---

## Ausführung mit Fable

Die inhaltlichen Tasks (2–17) werden an Fable-Subagents delegiert:
`Agent(subagent_type: "general-purpose", model: "fable", ...)`. Fable schreibt
Slides, Demo-Code, Assignments und Solutions.

**Nicht an Fable delegiert:** Task 1 (Umbenennung) und Task 18
(Gesamtintegration). Beides sind mechanische Repo-weite Operationen, bei
denen ein Subagent ohne Gesamtüberblick mehr Schaden als Nutzen stiftet.

Jeder Fable-Subagent bekommt im Prompt mit:

1. Den vollständigen Abschnitt **Global Constraints** dieses Plans (wörtlich).
2. Den vollständigen Task-Text inklusive `Interfaces`-Block.
3. Den Hinweis, die Verifikationsschritte selbst auszuführen und deren
   Ausgabe im Ergebnis zu berichten.

Nach jedem Subagent-Lauf prüft der Orchestrator die Verifikation selbst nach,
bevor committet wird.

---

## Task 1: Umbenennung und Struktur

Der einzige Task, der Bestehendes anfasst. Kein neuer Inhalt. Läuft
vollständig im Orchestrator, nicht in einem Subagent.

**Files:**

- Rename: `slides/00_Microservices_Architecture` → `slides/10_Microservices_Architecture`
- Rename: `slides/01_Configuration` → `slides/11_Configuration`
- Rename: `slides/02_Testing` → `slides/12_Testing`
- Rename: `slides/03_Data` → `slides/13_Data`
- Rename: `slides/04_Web` → `slides/14_Web`
- Rename: `slides/05_Actuator` → `slides/15_Actuator`
- Rename: `slides/06_Security` → `slides/16_Security`
- Rename: `slides/07_Messaging` → `slides/17_Messaging`
- Modify: `pom.xml`, `demos/pom.xml`, `assignments/pom.xml`,
  `solutions/pom.xml`
- Modify: alle 20 Modul-POMs mit `<parent><artifactId>sb-advanced-*`
- Modify: `slides/template.html`

**Interfaces:**

- Produces: Maven-Koordinaten `tech.erben:sb-training`,
  `tech.erben:sb-training-demos`, `tech.erben:sb-training-assignments`,
  `tech.erben:sb-training-solutions`. Alle folgenden Tasks setzen diese
  `artifactId`s in ihren `<parent>`-Blöcken voraus.

- [ ] **Schritt 1: Ausgangszustand sichern**

Der Arbeitsbaum enthält vor Beginn nicht committete Änderungen. Diese zuerst
klären — entweder committen oder stashen, damit der Rename-Commit sauber
bleibt:

```bash
cd /Users/aerben/repositories/it-erben/gfu/spring-boot-advanced
git status --short
```

Erwartung: Liste der Änderungen. Committe oder stashe sie, bis
`git status --short` leer ist. Nicht einfach überschreiben.

**Bekannter roter Test im Ausgangszustand.** Stand 2026-07-28 schlägt
`mvn test` in einem Modul fehl:

```text
tech.erben.security.oauth2.web.OAuth2DemoControllerTest
  .apiMeReturnsUserInfoWhenOAuth2LoggedIn
Caused by: Client id of registration 'github' must not be empty.
```

Der Test stammt aus dem noch untrackten Verzeichnis
`demos/sb-advanced-security-oauth2-demo/src/test/`. Er lädt den vollen
Anwendungskontext, findet aber keine OAuth2-Client-Registrierung. Behebbar
durch `src/test/resources/application.properties` in diesem Modul mit
Platzhalterwerten:

```properties
spring.security.oauth2.client.registration.github.client-id=test-client-id
spring.security.oauth2.client.registration.github.client-secret=test-secret
```

Das ist Bestandsarbeit, nicht Teil des Basis-Teils. Entweder vorab beheben
oder bewusst offen lassen — dann schlägt aber Task 18 Schritt 2 fehl und die
Prüfung muss auf die Basis-Module eingegrenzt werden. Die Entscheidung
gehört dem Nutzer; nicht ungefragt am Bestand herumreparieren.

- [ ] **Schritt 2: Slide-Verzeichnisse umbenennen**

```bash
git mv slides/00_Microservices_Architecture slides/10_Microservices_Architecture
git mv slides/01_Configuration              slides/11_Configuration
git mv slides/02_Testing                    slides/12_Testing
git mv slides/03_Data                       slides/13_Data
git mv slides/04_Web                        slides/14_Web
git mv slides/05_Actuator                   slides/15_Actuator
git mv slides/06_Security                   slides/16_Security
git mv slides/07_Messaging                  slides/17_Messaging
```

- [ ] **Schritt 3: Umbenennung prüfen**

```bash
ls slides/
```

Erwartung: genau `10_Microservices_Architecture`, `11_Configuration`,
`12_Testing`, `13_Data`, `14_Web`, `15_Actuator`, `16_Security`,
`17_Messaging`, `template.html`. Keine Verzeichnisse mit Präfix `0`.

```bash
find slides -name "*.svg" | wc -l
```

Erwartung: `9` (sechs Bilder in `10_`, eines in `12_`, zwei in `16_`).

- [ ] **Schritt 4: Root-POM umbenennen**

In `pom.xml`:

```xml
    <artifactId>sb-training</artifactId>
```

statt `sb-advanced`.

- [ ] **Schritt 5: Aggregator-POMs umbenennen**

In `demos/pom.xml`, `assignments/pom.xml` und `solutions/pom.xml` jeweils die
eigene `artifactId` und den Parent anpassen. Beispiel `demos/pom.xml`:

```xml
    <artifactId>sb-training-demos</artifactId>
    <packaging>pom</packaging>

    <parent>
        <groupId>tech.erben</groupId>
        <artifactId>sb-training</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>
```

Analog `sb-training-assignments` und `sb-training-solutions`.

- [ ] **Schritt 6: Parent-Referenzen in den Modul-POMs anpassen**

20 Modul-POMs referenzieren die alten Aggregator-Namen. Ersetzen per `sed`,
danach kontrollieren:

```bash
find demos assignments solutions -name pom.xml -not -path "*/target/*" \
  -exec sed -i '' \
  -e 's|<artifactId>sb-advanced-demos</artifactId>|<artifactId>sb-training-demos</artifactId>|' \
  -e 's|<artifactId>sb-advanced-assignments</artifactId>|<artifactId>sb-training-assignments</artifactId>|' \
  -e 's|<artifactId>sb-advanced-solutions</artifactId>|<artifactId>sb-training-solutions</artifactId>|' {} +
```

Achtung: Diese Ersetzung trifft in `demos/pom.xml`,
`assignments/pom.xml` und `solutions/pom.xml` auch die eigene `artifactId` —
das ist gewollt und deckt sich mit Schritt 5.

```bash
grep -rn "sb-advanced-demos\|sb-advanced-assignments\|sb-advanced-solutions\|>sb-advanced<" \
  --include=pom.xml . | grep -v target
```

Erwartung: keine Treffer.

- [ ] **Schritt 7: Reaktor prüfen**

```bash
mvn -B -q validate
```

Erwartung: Exit-Code 0, keine Ausgabe. Bei `Non-resolvable parent POM` ist in
Schritt 5 oder 6 ein POM übersehen worden.

- [ ] **Schritt 8: Vollbuild prüfen**

```bash
mvn -B -DskipTests package
```

Erwartung: `BUILD SUCCESS` mit **40** Modulen im Reactor Summary — genauso
viele wie vor der Umbenennung. Zählen:

```bash
mvn -B -DskipTests package 2>&1 | awk '/Reactor Summary/,/BUILD SUCCESS/' \
  | grep -c "SUCCESS \["
```

Erwartung: `40`. Eine abweichende Zahl heißt, dass ein Modul aus dem Reactor
gefallen ist.

- [ ] **Schritt 9: `template.html` anpassen**

In `slides/template.html`:

```html
    <title>Spring Boot Schulung</title>
```

und

```html
<h1>Spring Boot Schulung</h1>
```

Der Rest der Datei bleibt unverändert — die Linkliste wird vom
PDF-Publisher befüllt, es gibt keine hartkodierten Modulverweise.

- [ ] **Schritt 10: Marp-Header der Advanced-Decks prüfen**

```bash
grep -h "^header:" slides/*/slides.md | sort -u
```

Erwartung: nur `header: Spring Boot Advanced`. Die Advanced-Decks behalten
ihren Header — hier ist nichts zu ändern, der Schritt bestätigt nur den
Ausgangszustand für die Basis-Decks.

- [ ] **Schritt 11: Linter laufen lassen**

```bash
npx --yes markdownlint-cli2 "slides/**/*.md"
```

Erwartung: `Summary: 0 issues`.

- [ ] **Schritt 12: Committen**

```bash
git add -A
git commit -m "refactor: Slides auf 10-17 umnummerieren, Maven auf sb-training umbenennen"
```

---

## Task 2: Demo Modul 00 — Spring Core

Vor den Slides, damit das Deck echten Code zeigen kann statt erfundenen.

**Files:**

- Create: `demos/sb-basics-core-demo/pom.xml`
- Create: `demos/sb-basics-core-demo/sb-basics-core-demo-start/pom.xml`
- Create: `demos/sb-basics-core-demo/sb-basics-core-demo-finished/pom.xml`
- Create: unter beiden Modulen jeweils
  `src/main/java/tech/erben/springboot/basics/core/` mit den unten
  aufgeführten Klassen
- Create: `demos/sb-basics-core-demo/README.md`
- Modify: `demos/pom.xml` — `<module>sb-basics-core-demo</module>`

**Interfaces:**

- Consumes: Parent `tech.erben:sb-training-demos:1.0.0-SNAPSHOT` (Task 1).
- Produces: Das Klassenmodell der Buchhandlung, das Task 7 (Web-Demo) und
  Task 10 (JPA-Demo) wiederverwenden:
  - `record Book(String isbn, String title, BigDecimal netPrice)`
  - `interface BookRepository { List<Book> findAll(); Optional<Book> findByIsbn(String isbn); }`

**Dependencies im `-start`- und `-finished`-POM:** nur
`spring-boot-starter` (kein Web, kein Data — Modul 00 zeigt den Container,
nicht das Framework drumherum) plus `spring-boot-starter-test` in
`test`-Scope.

- [ ] **Schritt 1: Aggregator-POM anlegen**

`demos/sb-basics-core-demo/pom.xml`, Muster exakt wie
`demos/sb-advanced-web-demo/pom.xml`:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>tech.erben</groupId>
        <artifactId>sb-training-demos</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>sb-basics-core-demo</artifactId>
    <packaging>pom</packaging>

    <modules>
        <module>sb-basics-core-demo-start</module>
        <module>sb-basics-core-demo-finished</module>
    </modules>
</project>
```

- [ ] **Schritt 2: `-finished` implementieren**

Klassen unter `tech.erben.springboot.basics.core`:

| Klasse | Rolle im Deck |
| --- | --- |
| `CoreDemoApplication` | `@SpringBootApplication`, Einstiegspunkt |
| `Book` | Record: `isbn`, `title`, `netPrice` (`BigDecimal`) |
| `BookRepository` | Interface: `findAll()`, `findByIsbn(String)` |
| `InMemoryBookRepository` | `@Repository`, drei feste Bücher |
| `PriceCalculator` | Interface: `BigDecimal calculate(Book book)` |
| `NetPriceCalculator` | `@Component("netPriceCalculator")`, gibt `netPrice` zurück |
| `GrossPriceCalculator` | `@Component("grossPriceCalculator")`, `@Primary`, rechnet 19 % auf |
| `BookService` | `@Service`, Konstruktor-Injection von `BookRepository` + `PriceCalculator` |
| `ShippingConfig` | `@Configuration` mit `@Bean Clock clock()` — zeigt Bereitstellung einer Fremdklasse |
| `ShopProperties` | `@Component` mit `@Value("${shop.name:Buchhandlung Erben}")` |
| `CatalogRunner` | `@Component implements CommandLineRunner`, gibt Katalog mit Bruttopreisen aus |
| `PrototypeCounter` | `@Component @Scope("prototype")`, zeigt Scope-Unterschied |

`BookService` demonstriert die Auflösung der Mehrdeutigkeit: Der Konstruktor
nimmt `PriceCalculator` ohne Qualifier und bekommt durch `@Primary` den
`GrossPriceCalculator`. Eine zweite Methode nimmt per
`@Qualifier("netPriceCalculator")` explizit den anderen.

`src/main/resources/application.properties`:

```properties
shop.name=Buchhandlung Erben
spring.main.banner-mode=off
```

- [ ] **Schritt 3: Test für `-finished` schreiben**

`src/test/java/tech/erben/springboot/basics/core/BookServiceTest.java`:

```java
package tech.erben.springboot.basics.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("Der Katalog enthaelt die drei Beispielbuecher")
    void catalogContainsThreeBooks() {
        assertThat(bookService.findAll()).hasSize(3);
    }

    @Test
    @DisplayName("Ohne Qualifier greift der @Primary GrossPriceCalculator")
    void defaultCalculatorAddsVat() {
        Book book = new Book("978-3-16-148410-0", "Spring im Einsatz",
                new BigDecimal("100.00"));

        assertThat(bookService.priceFor(book))
                .isEqualByComparingTo(new BigDecimal("119.00"));
    }

    @Test
    @DisplayName("Mit Qualifier greift der NetPriceCalculator")
    void qualifiedCalculatorReturnsNetPrice() {
        Book book = new Book("978-3-16-148410-0", "Spring im Einsatz",
                new BigDecimal("100.00"));

        assertThat(bookService.netPriceFor(book))
                .isEqualByComparingTo(new BigDecimal("100.00"));
    }
}
```

Daraus ergibt sich die Signatur von `BookService`: `List<Book> findAll()`,
`BigDecimal priceFor(Book)`, `BigDecimal netPriceFor(Book)`.

- [ ] **Schritt 4: Test laufen lassen**

```bash
mvn -B -pl demos/sb-basics-core-demo/sb-basics-core-demo-finished -am test
```

Erwartung: `Tests run: 3, Failures: 0, Errors: 0`.

- [ ] **Schritt 5: `-start` als reduzierte Kopie anlegen**

`-start` enthält dieselben Klassen und dieselbe Verzeichnisstruktur, aber:

- Keine Stereotyp-Annotationen (`@Component`, `@Service`, `@Repository`,
  `@Configuration`, `@Bean`, `@Primary`, `@Qualifier`, `@Value`, `@Scope`).
- Statt jeder entfernten Annotation ein Kommentar
  `// TODO: Modul 00 — als Spring-Bean deklarieren`.
- `CoreDemoApplication` behält `@SpringBootApplication`, damit das Modul
  startet.
- `CatalogRunner` und `BookService` bleiben als Klassen vorhanden, werden
  aber nicht instanziiert.
- **Kein Test.** Ein `-start` ohne Verdrahtung würde jeden `@SpringBootTest`
  rot werfen; das ist Demo-Ausgangsmaterial, kein Assignment.

- [ ] **Schritt 6: `-start` muss kompilieren und starten**

```bash
mvn -B -pl demos/sb-basics-core-demo/sb-basics-core-demo-start -am package
```

Erwartung: `BUILD SUCCESS`.

- [ ] **Schritt 7: Demo-README schreiben**

`demos/sb-basics-core-demo/README.md`: was die Demo zeigt, in welcher
Reihenfolge live verdrahtet wird (Repository → Service → Runner →
Mehrdeutigkeit → Scopes), und wie beide Varianten gestartet werden
(`mvn spring-boot:run` im jeweiligen Modul).

- [ ] **Schritt 8: In `demos/pom.xml` eintragen**

```xml
        <module>sb-basics-core-demo</module>
```

- [ ] **Schritt 9: Gesamtbuild**

```bash
mvn -B -DskipTests package
```

Erwartung: `BUILD SUCCESS`, Modulzahl um 3 gestiegen.

- [ ] **Schritt 10: Committen**

```bash
git add demos/sb-basics-core-demo demos/pom.xml
git commit -m "feat(basics): Demo fuer Modul 00 Spring Core"
```

---

## Task 3: Slides Modul 00 — Spring Core

**Files:**

- Create: `slides/00_Spring_Core/slides.md`
- Create: `slides/00_Spring_Core/images/` (leer anlegen, `.gitkeep`)

**Interfaces:**

- Consumes: Die Klassennamen aus Task 2 (`BookService`, `BookRepository`,
  `InMemoryBookRepository`, `PriceCalculator`, `GrossPriceCalculator`,
  `NetPriceCalculator`, `ShippingConfig`). Code-Beispiele auf den Slides
  müssen zum Demo-Code passen — abweichende Namen sind ein Fehler.

- [ ] **Schritt 1: Deck schreiben**

Gliederung, 2,5 Unterrichtsstunden:

```text
# Spring Core: Der Container
  ## In diesem Modul
  ## Das Problem: Objekte selbst verdrahten
  ## Inversion of Control
  ## Dependency Injection
  ## Der ApplicationContext
# Beans deklarieren
  ## @Component und die Stereotypen
  ## Component Scan
  ## @Configuration und @Bean
  ## Wann @Component, wann @Bean?
# Beans injizieren
  ## Konstruktor-Injection
  ## Warum nicht Feld-Injection?
  ## Mehrere Kandidaten: @Primary
  ## Mehrere Kandidaten: @Qualifier
  ## Optionale Abhaengigkeiten
# Bean Scopes und Lifecycle
  ## Singleton (Default)
  ## Prototype
  ## Request und Session (nur erwaehnt)
  ## @PostConstruct und @PreDestroy
# Konfigurationswerte
  ## @Value
  ## Defaults und Platzhalter
# Maven-Grundlagen
  ## Aufbau einer pom.xml
  ## Parent-POM und Vererbung
  ## Eine Dependency aufnehmen
  ## Der Reaktor
# Demo
  ## Was wir gleich bauen
# Uebung
  ## Assignment 00
```

Jede Konzept-Slide bringt ein lauffähiges Code-Beispiel aus der Buchhandlung.
Die Slide „Warum nicht Feld-Injection?" nennt drei konkrete Gründe:
Testbarkeit ohne Container, keine `final`-Felder, verstecke Abhängigkeiten.

- [ ] **Schritt 2: Code-Beispiele gegen die Demo prüfen**

```bash
grep -o "class [A-Za-z]*\|interface [A-Za-z]*" slides/00_Spring_Core/slides.md | sort -u
```

Jeder Typname muss in
`demos/sb-basics-core-demo/sb-basics-core-demo-finished/src/main/java/` als
Datei existieren. Abweichungen korrigieren.

- [ ] **Schritt 3: Frontmatter prüfen**

```bash
head -7 slides/00_Spring_Core/slides.md
```

Erwartung: exakt der Frontmatter-Block aus den Global Constraints mit
`header: Spring Boot Basics`.

- [ ] **Schritt 4: Linten**

```bash
npx --yes markdownlint-cli2 "slides/00_Spring_Core/slides.md"
```

Erwartung: `Summary: 0 issues`.

- [ ] **Schritt 5: Umfang prüfen**

```bash
wc -l slides/00_Spring_Core/slides.md
```

Erwartung: 400–600 Zeilen. Deutlich darunter heißt: zu dünn für 2,5 Stunden.

- [ ] **Schritt 6: Committen**

```bash
git add slides/00_Spring_Core
git commit -m "feat(basics): Slides fuer Modul 00 Spring Core"
```

---

## Task 4: Assignment und Solution Modul 00

**Files:**

- Create: `assignments/sb-basics-core-assignment/` (POM, README, `src/`)
- Create: `solutions/sb-basics-core-solution/` (POM, `src/`)
- Modify: `assignments/pom.xml`, `solutions/pom.xml`

**Interfaces:**

- Consumes: Parent `sb-training-assignments` bzw. `sb-training-solutions`.
- Produces: Fachlichkeit Kursverwaltung, die Task 9, 12 und 15
  weiterverwenden:
  - `record Course(String code, String title, int seats, BigDecimal netFee)`
  - `record Trainer(String name, String email)`

- [ ] **Schritt 1: Assignment-Modul anlegen**

POM nach dem Muster von
`assignments/sb-advanced-data-jpa-assignment/pom.xml`, aber **ohne Lombok**
und mit `spring-boot-starter` statt `spring-boot-starter-data-jpa`. Parent
ist `sb-training-assignments`.

Package `tech.erben.springboot.basics.core.task`.

Bereitgestellte Klassen (alle **ohne** Spring-Annotationen, mit TODO-Marken):

| Klasse | Zustand im Assignment |
| --- | --- |
| `CourseAdminApplication` | fertig, `@SpringBootApplication` |
| `Course` | fertig, Record |
| `Trainer` | fertig, Record |
| `CourseCatalog` | Interface, fertig |
| `InMemoryCourseCatalog` | Implementierung fertig, Annotation fehlt |
| `FeeCalculator` | Interface `BigDecimal calculate(Course)`, fertig |
| `NetFeeCalculator` | Logik fertig, Annotation fehlt |
| `GrossFeeCalculator` | Logik fertig, Annotation und `@Primary` fehlen |
| `TrainerDirectory` | Fremdklasse ohne Annotationen, muss per `@Bean` bereitgestellt werden |
| `CourseService` | Felder und Methodenrümpfe da, Konstruktor-Injection fehlt |

- [ ] **Schritt 2: Assignment-Tests schreiben**

`src/test/java/tech/erben/springboot/basics/core/task/CourseServiceTest.java`:

```java
package tech.erben.springboot.basics.core.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private TrainerDirectory trainerDirectory;

    @Test
    @DisplayName("Aufgabe 1: Der Katalog ist als Bean verdrahtet")
    void catalogIsWired() {
        assertThat(courseService.findAll()).hasSize(4);
    }

    @Test
    @DisplayName("Aufgabe 2: Ohne Qualifier greift der Brutto-Rechner")
    void defaultCalculatorAddsVat() {
        Course course = new Course("SB-BASIC", "Spring Boot Basics", 12,
                new BigDecimal("1000.00"));

        assertThat(courseService.feeFor(course))
                .isEqualByComparingTo(new BigDecimal("1190.00"));
    }

    @Test
    @DisplayName("Aufgabe 3: Mit Qualifier greift der Netto-Rechner")
    void qualifiedCalculatorReturnsNetFee() {
        Course course = new Course("SB-BASIC", "Spring Boot Basics", 12,
                new BigDecimal("1000.00"));

        assertThat(courseService.netFeeFor(course))
                .isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("Aufgabe 4: TrainerDirectory kommt aus einer @Bean-Methode")
    void trainerDirectoryIsProvidedByConfiguration() {
        assertThat(trainerDirectory.findByEmail("anna@example.com"))
                .isPresent();
    }
}
```

- [ ] **Schritt 3: Tests müssen rot sein**

```bash
mvn -B -pl assignments/sb-basics-core-assignment -am test
```

Erwartung: **FAIL**. Konkret `UnsatisfiedDependencyException` bzw.
`NoSuchBeanDefinitionException` für `CourseService`, weil keine der Klassen
als Bean deklariert ist. Läuft der Test grün, ist versehentlich schon eine
Annotation gesetzt — dann entfernen.

- [ ] **Schritt 4: README nach Template schreiben**

`assignments/sb-basics-core-assignment/README.md` mit vier nummerierten
Aufgaben, die genau den vier Tests entsprechen, plus Bonusaufgabe
(`@Scope("prototype")` an einer Zählerklasse demonstrieren) und
Erfolgskriterien als Checkliste. Der Abschnitt „Lösung" verweist auf
`solutions/sb-basics-core-solution`.

- [ ] **Schritt 5: Solution als Kopie mit ausgefülltem Code anlegen**

```bash
cp -R assignments/sb-basics-core-assignment solutions/sb-basics-core-solution
rm -rf solutions/sb-basics-core-solution/target
rm solutions/sb-basics-core-solution/README.md
```

Dann in der Kopie: `artifactId` auf `sb-basics-core-solution` ändern, Parent
auf `sb-training-solutions`, alle TODO-Marken durch die richtigen
Annotationen ersetzen. Die Tests bleiben unverändert — sie sind der Beweis.

- [ ] **Schritt 6: Solution muss grün sein**

```bash
mvn -B -pl solutions/sb-basics-core-solution -am test
```

Erwartung: `Tests run: 4, Failures: 0, Errors: 0`.

- [ ] **Schritt 7: Beide POMs registrieren**

`assignments/pom.xml`: `<module>sb-basics-core-assignment</module>`
`solutions/pom.xml`: `<module>sb-basics-core-solution</module>`

- [ ] **Schritt 8: Reaktor darf nicht rot werden**

Das Assignment enthält absichtlich rote Tests. Damit `mvn verify` im
Gesamtbuild nicht bricht, überspringt das Assignment-POM sie — aber über
eine **eigene Property**, damit die Prüfung von außen wieder eingeschaltet
werden kann. Ein hartkodiertes `<skipTests>true</skipTests>` ließe sich per
Kommandozeile nicht mehr aushebeln.

```xml
    <properties>
        <!-- Die Tests sind Aufgabenstellung, nicht Regression. Im
             Gesamtbuild werden sie uebersprungen; die Absicherung
             uebernimmt solutions/sb-basics-core-solution. Zum Pruefen
             des roten Ausgangszustands: -DskipAssignmentTests=false -->
        <skipAssignmentTests>true</skipAssignmentTests>
    </properties>
```

und im `<build><plugins>`-Block:

```xml
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <skipTests>${skipAssignmentTests}</skipTests>
                </configuration>
            </plugin>
```

Dasselbe Muster gilt für alle vier Assignments.

Prüfen, dass der Schalter wirkt:

```bash
mvn -B -pl assignments/sb-basics-core-assignment test
mvn -B -pl assignments/sb-basics-core-assignment test -DskipAssignmentTests=false
```

Erwartung: erster Lauf `Tests are skipped`, zweiter Lauf **FAIL**.

```bash
mvn -B verify
```

Erwartung: `BUILD SUCCESS`.

- [ ] **Schritt 9: Committen**

```bash
git add assignments/sb-basics-core-assignment solutions/sb-basics-core-solution \
        assignments/pom.xml solutions/pom.xml
git commit -m "feat(basics): Uebung und Loesung fuer Modul 00 Spring Core"
```

---

## Task 5: Demo Modul 01 — Spring Boot Basics

**Files:**

- Create: `demos/sb-basics-boot-demo/pom.xml` (einzelnes Modul, kein
  Aggregator)
- Create: `demos/sb-basics-boot-demo/src/main/java/tech/erben/springboot/basics/boot/`
- Create: `demos/sb-basics-boot-demo/src/main/resources/application.properties`,
  `application-dev.properties`, `application-prod.properties`
- Create: `demos/sb-basics-boot-demo/README.md`
- Modify: `demos/pom.xml`

**Interfaces:**

- Produces: `record ShopProperties(String name, String currency, int pageSize)`
  als `@ConfigurationProperties("shop")` — Task 17 (Slides Betrieb) verweist
  darauf.

**Dependencies:** `spring-boot-starter-web`,
`spring-boot-configuration-processor` (optional, `provided`),
`spring-boot-devtools` (`runtime`, `optional`), `spring-boot-starter-test`.

- [ ] **Schritt 1: Modul anlegen**

Klassen unter `tech.erben.springboot.basics.boot`:

| Klasse | Zweck |
| --- | --- |
| `BootDemoApplication` | `@SpringBootApplication`, `@ConfigurationPropertiesScan` |
| `ShopProperties` | Record mit `@ConfigurationProperties("shop")` |
| `ShopInfoController` | `@RestController`, `GET /info` gibt Properties + aktive Profile zurück |
| `DevDataInitializer` | `@Component`, `@Profile("dev")`, loggt beim Start |

`application.properties`:

```properties
spring.application.name=bookstore
shop.name=Buchhandlung Erben
shop.currency=EUR
shop.page-size=20
```

`application-dev.properties` überschreibt `shop.page-size=5`,
`application-prod.properties` setzt `shop.page-size=50`.

- [ ] **Schritt 2: Test schreiben**

`src/test/java/tech/erben/springboot/basics/boot/ShopPropertiesTest.java`:

```java
package tech.erben.springboot.basics.boot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("Profil dev ueberschreibt die Basis-Properties")
class ShopPropertiesTest {

    @Autowired
    private ShopProperties shopProperties;

    @Test
    void devProfileOverridesPageSize() {
        assertThat(shopProperties.pageSize()).isEqualTo(5);
        assertThat(shopProperties.name()).isEqualTo("Buchhandlung Erben");
    }
}
```

- [ ] **Schritt 3: Test laufen lassen**

```bash
mvn -B -pl demos/sb-basics-boot-demo -am test
```

Erwartung: `Tests run: 1, Failures: 0, Errors: 0`.

- [ ] **Schritt 4: Fat-JAR bauen und starten**

```bash
mvn -B -pl demos/sb-basics-boot-demo -am package
java -jar demos/sb-basics-boot-demo/target/sb-basics-boot-demo-1.0.0-SNAPSHOT.jar \
     --server.port=8081 --shop.page-size=99 &
sleep 12
curl -s localhost:8081/info
kill %1
```

Erwartung: JSON mit `"pageSize":99` — belegt, dass Kommandozeilenargumente
Properties überschreiben. Das ist die Demo-Pointe des Moduls.

- [ ] **Schritt 5: README, POM-Eintrag, Gesamtbuild, Commit**

```bash
git add demos/sb-basics-boot-demo demos/pom.xml
git commit -m "feat(basics): Demo fuer Modul 01 Spring Boot Basics"
```

---

## Task 6: Slides Modul 01 — Spring Boot Basics

**Files:**

- Create: `slides/01_Spring_Boot_Basics/slides.md`, `images/.gitkeep`

**Interfaces:**

- Consumes: `ShopProperties`, `ShopInfoController`, die Property-Dateien aus
  Task 5.

- [ ] **Schritt 1: Deck schreiben**

```text
# Spring Boot: Was Boot ueber Spring legt
  ## In diesem Modul
  ## Ohne Boot: der Aufwand
  ## Die vier Versprechen von Boot
# Starter
  ## Was ist ein Starter?
  ## Die wichtigsten Starter
  ## spring-boot-dependencies als BOM
  ## Versionen: was Boot fuer uns verwaltet
# AutoConfiguration
  ## Die Idee: Classpath entscheidet
  ## Beispiel: spring-boot-starter-web
  ## Was passiert beim Start?
  ## Eigene Beans gewinnen immer
  ## Debuggen: --debug und das Condition-Report
# @SpringBootApplication
  ## Drei Annotationen in einer
  ## Warum die Paketstruktur zaehlt
# Konfiguration
  ## application.properties und application.yaml
  ## @Value vs. @ConfigurationProperties
  ## @ConfigurationProperties mit Records
  ## Reihenfolge: wer gewinnt?
  ## Profile
  ## Profile aktivieren
# Entwicklung und Betriebsarten
  ## Spring Initializr
  ## Projektstruktur
  ## DevTools
  ## spring-boot:run vs. Fat-JAR
# Demo
  ## Was wir gleich ansehen
```

Die Slide „Reihenfolge: wer gewinnt?" zeigt die für Einsteiger relevanten
fünf Quellen (Kommandozeile, Environment, Profil-Properties,
`application.properties`, Defaults) und verweist darauf, dass
`11_Configuration` die vollständige Liste bringt.

- [ ] **Schritt 2: Typnamen gegen die Demo prüfen**

```bash
grep -o "class [A-Za-z]*\|record [A-Za-z]*" slides/01_Spring_Boot_Basics/slides.md | sort -u
```

Jeder Typ muss in `demos/sb-basics-boot-demo/src/main/java/` existieren.

- [ ] **Schritt 3: Frontmatter, Lint, Umfang**

```bash
head -7 slides/01_Spring_Boot_Basics/slides.md
npx --yes markdownlint-cli2 "slides/01_Spring_Boot_Basics/slides.md"
wc -l slides/01_Spring_Boot_Basics/slides.md
```

Erwartung: `header: Spring Boot Basics`, `0 issues`, 400–600 Zeilen.

- [ ] **Schritt 4: Committen**

```bash
git add slides/01_Spring_Boot_Basics
git commit -m "feat(basics): Slides fuer Modul 01 Spring Boot Basics"
```

---

## Task 7: Demo Modul 02 — Web und REST

**Files:**

- Create: `demos/sb-basics-web-demo/pom.xml` (Aggregator)
- Create: `demos/sb-basics-web-demo/sb-basics-web-demo-start/`
- Create: `demos/sb-basics-web-demo/sb-basics-web-demo-finished/`
- Create: `demos/sb-basics-web-demo/README.md`
- Modify: `demos/pom.xml`

**Interfaces:**

- Consumes: `Book` aus Task 2 (gleiche Felder, eigenes Package
  `…basics.web`).
- Produces: REST-Vertrag, auf den Task 13 (Testing-Demo) aufsetzt:
  - `GET /api/books` → `200`, Liste von `BookResponse`
  - `GET /api/books/{isbn}` → `200` oder `404`
  - `POST /api/books` → `201` mit `Location`-Header, `400` bei
    Validierungsfehler
  - `DELETE /api/books/{isbn}` → `204` oder `404`
  - `record BookRequest(String isbn, String title, BigDecimal netPrice)`
  - `record BookResponse(String isbn, String title, BigDecimal netPrice, BigDecimal grossPrice)`

**Dependencies:** `spring-boot-starter-web`,
`spring-boot-starter-validation`, `spring-boot-starter-test`.

- [ ] **Schritt 1: `-finished` implementieren**

Package `tech.erben.springboot.basics.web`:

| Klasse | Zweck |
| --- | --- |
| `WebDemoApplication` | Einstiegspunkt |
| `Book` | Record |
| `BookRequest` | Record mit `@NotBlank isbn`, `@NotBlank @Size(max = 200) title`, `@NotNull @Positive netPrice` |
| `BookResponse` | Record inkl. berechnetem `grossPrice` |
| `BookService` | In-Memory-Verwaltung, wirft `BookNotFoundException` |
| `BookNotFoundException` | `RuntimeException` mit ISBN |
| `BookController` | `@RestController`, `@RequestMapping("/api/books")` |
| `RestExceptionHandler` | `@RestControllerAdvice`, mappt `BookNotFoundException` → `404` und `MethodArgumentNotValidException` → `400` |

- [ ] **Schritt 2: Test schreiben**

`src/test/java/tech/erben/springboot/basics/web/BookControllerTest.java`:

```java
package tech.erben.springboot.basics.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/books liefert 200 und den Bruttopreis")
    void listReturnsBooksWithGrossPrice() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].grossPrice").exists());
    }

    @Test
    @DisplayName("GET auf unbekannte ISBN liefert 404")
    void unknownIsbnReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/books/999-does-not-exist"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST mit gueltigem Body liefert 201 und Location")
    void createReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"978-0-13-468599-1",
                                 "title":"Effective Java",
                                 "netPrice":49.99}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "/api/books/978-0-13-468599-1"));
    }

    @Test
    @DisplayName("POST ohne Titel liefert 400")
    void createWithoutTitleReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"978-0-13-468599-1",
                                 "title":"",
                                 "netPrice":49.99}
                                """))
                .andExpect(status().isBadRequest());
    }
}
```

- [ ] **Schritt 3: Test laufen lassen**

```bash
mvn -B -pl demos/sb-basics-web-demo/sb-basics-web-demo-finished -am test
```

Erwartung: `Tests run: 4, Failures: 0, Errors: 0`.

- [ ] **Schritt 4: `-start` anlegen**

Enthält `WebDemoApplication`, `Book`, `BookService`,
`BookNotFoundException` fertig. `BookController`, `BookRequest`,
`BookResponse` und `RestExceptionHandler` fehlen komplett — die entstehen
live. Kein Test im `-start`.

```bash
mvn -B -pl demos/sb-basics-web-demo/sb-basics-web-demo-start -am package
```

Erwartung: `BUILD SUCCESS`.

- [ ] **Schritt 5: README, POM-Eintrag, Gesamtbuild, Commit**

```bash
git add demos/sb-basics-web-demo demos/pom.xml
git commit -m "feat(basics): Demo fuer Modul 02 Web und REST"
```

---

## Task 8: Slides Modul 02 — Web und REST

**Files:**

- Create: `slides/02_Web_REST/slides.md`, `images/.gitkeep`

**Interfaces:**

- Consumes: Klassen und REST-Vertrag aus Task 7.

- [ ] **Schritt 1: Deck schreiben**

```text
# RESTful Services mit Spring Boot
  ## In diesem Modul
  ## Was REST bedeutet
  ## Ressourcen, Verben, Statuscodes
# Der Controller
  ## @RestController
  ## @GetMapping und Verwandte
  ## @PathVariable
  ## @RequestParam
  ## @RequestBody
  ## ResponseEntity
  ## Welchen Statuscode wann?
# JSON
  ## Jackson: Serialisierung ohne Zutun
  ## Records als DTOs
  ## Warum DTOs statt Entities?
  ## Felder umbenennen und ausblenden
# Validierung
  ## Bean Validation einbinden
  ## Die wichtigsten Constraints
  ## @Valid am Controller
  ## Was passiert bei einem Verstoss?
# Fehlerbehandlung
  ## @ExceptionHandler im Controller
  ## @RestControllerAdvice global
  ## Eine eigene Fehlerantwort bauen
# HTTP aufrufen
  ## RestClient
  ## GET und POST mit RestClient
# Demo
  ## Was wir gleich bauen
# Uebung
  ## Assignment 02
```

Die Slide „Warum DTOs statt Entities?" nimmt vorweg, was in `03_Data_JPA`
sonst schmerzt: Entities tragen Lazy-Proxies und Persistenz-Details, die
nicht ins JSON gehören.

- [ ] **Schritt 2: Typnamen prüfen, Frontmatter, Lint, Umfang**

```bash
grep -o "class [A-Za-z]*\|record [A-Za-z]*" slides/02_Web_REST/slides.md | sort -u
head -7 slides/02_Web_REST/slides.md
npx --yes markdownlint-cli2 "slides/02_Web_REST/slides.md"
wc -l slides/02_Web_REST/slides.md
```

- [ ] **Schritt 3: Committen**

```bash
git add slides/02_Web_REST
git commit -m "feat(basics): Slides fuer Modul 02 Web und REST"
```

---

## Task 9: Assignment und Solution Modul 02

**Files:**

- Create: `assignments/sb-basics-web-assignment/`
- Create: `solutions/sb-basics-web-solution/`
- Modify: `assignments/pom.xml`, `solutions/pom.xml`

**Interfaces:**

- Consumes: `Course` aus Task 4.
- Produces: REST-Vertrag der Kursverwaltung, den Task 15 wiederverwendet:
  - `GET /api/courses`, `GET /api/courses/{code}`, `POST /api/courses`,
    `DELETE /api/courses/{code}`
  - `record CourseRequest(String code, String title, int seats, BigDecimal netFee)`
  - `record CourseResponse(String code, String title, int seats, BigDecimal netFee, BigDecimal grossFee)`

- [ ] **Schritt 1: Assignment anlegen**

Package `tech.erben.springboot.basics.web.task`. Bereitgestellt und fertig:
`CourseAdminApplication`, `Course`, `CourseService` (In-Memory, wirft
`CourseNotFoundException`), `CourseNotFoundException`.

Zu bauen von den Teilnehmern: `CourseRequest`, `CourseResponse`,
`CourseController`, `RestExceptionHandler`.

Vier Aufgaben, die den vier Tests entsprechen:

1. `CourseResponse` und `GET /api/courses` mit berechnetem `grossFee`
2. `GET /api/courses/{code}` mit `404` bei Unbekanntem
3. `POST /api/courses` mit `201` und `Location`-Header
4. `CourseRequest` validieren und per `@RestControllerAdvice` `400` liefern

- [ ] **Schritt 2: Tests schreiben**

Struktur analog zu `BookControllerTest` aus Task 7, aber auf `/api/courses`
und die Kursverwaltung gemünzt. Vier Tests mit `@DisplayName` in der Form
`"Aufgabe N: …"`, damit die Zuordnung zur README eindeutig ist:

```java
package tech.erben.springboot.basics.web.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Aufgabe 1: GET /api/courses liefert 200 und grossFee")
    void listReturnsCoursesWithGrossFee() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].grossFee").exists());
    }

    @Test
    @DisplayName("Aufgabe 2: Unbekannter Kurscode liefert 404")
    void unknownCodeReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/courses/GIBT-ES-NICHT"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Aufgabe 3: POST liefert 201 und Location")
    void createReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"K8S-INTRO",
                                 "title":"Kubernetes Einstieg",
                                 "seats":10,
                                 "netFee":1400.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "/api/courses/K8S-INTRO"));
    }

    @Test
    @DisplayName("Aufgabe 4: POST ohne Titel liefert 400")
    void createWithoutTitleReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"K8S-INTRO",
                                 "title":"",
                                 "seats":10,
                                 "netFee":1400.00}
                                """))
                .andExpect(status().isBadRequest());
    }
}
```

- [ ] **Schritt 3: Tests müssen rot sein**

```bash
mvn -B -pl assignments/sb-basics-web-assignment -am test -DskipAssignmentTests=false
```

Erwartung: **FAIL** — `404` statt `200` bei Aufgabe 1, weil kein Controller
existiert.

Hinweis: Das Assignment-POM enthält wie in Task 4 die
Surefire-`skipTests`-Konfiguration. `-DskipAssignmentTests=false` hebelt sie
für die lokale Prüfung aus; ohne diesen Schalter würden die Tests
übersprungen und das Rot bliebe unsichtbar.

- [ ] **Schritt 4: README nach Template**

Vier Aufgaben, Bonusaufgabe (`PUT /api/courses/{code}` mit `200`/`404`),
Erfolgskriterien, Verweis auf `solutions/sb-basics-web-solution`.

- [ ] **Schritt 5: Solution anlegen und grün prüfen**

Kopie wie in Task 4 Schritt 5, Controller und Advice ausimplementiert,
Surefire-Skip **entfernt**.

```bash
mvn -B -pl solutions/sb-basics-web-solution -am test
```

Erwartung: `Tests run: 4, Failures: 0, Errors: 0`.

- [ ] **Schritt 6: Registrieren, Gesamtbuild, Commit**

```bash
mvn -B verify
git add assignments/sb-basics-web-assignment solutions/sb-basics-web-solution \
        assignments/pom.xml solutions/pom.xml
git commit -m "feat(basics): Uebung und Loesung fuer Modul 02 Web und REST"
```

---

## Task 10: Demo Modul 03 — Data und JPA

**Files:**

- Create: `demos/sb-basics-data-jpa-demo/pom.xml` (Aggregator)
- Create: `demos/sb-basics-data-jpa-demo/sb-basics-data-jpa-demo-start/`
- Create: `demos/sb-basics-data-jpa-demo/sb-basics-data-jpa-demo-finished/`
- Create: `demos/sb-basics-data-jpa-demo/README.md`
- Modify: `demos/pom.xml`

**Interfaces:**

- Produces: JPA-Modell, auf das Task 13 aufsetzt:
  - `@Entity class Book` mit `Long id`, `String isbn`, `String title`,
    `BigDecimal netPrice`, `Author author` (`@ManyToOne`)
  - `@Entity class Author` mit `Long id`, `String name`,
    `List<Book> books` (`@OneToMany(mappedBy = "author")`)
  - `interface BookRepository extends JpaRepository<Book, Long>` mit
    `Optional<Book> findByIsbn(String isbn)`,
    `List<Book> findByTitleContainingIgnoreCase(String fragment)`,
    `List<Book> findByNetPriceLessThan(BigDecimal limit)`,
    `List<Book> findByAuthorName(String name)` (`@Query` mit JPQL)

**Dependencies:** `spring-boot-starter-data-jpa`, `com.h2database:h2`
(`runtime`), `spring-boot-starter-test`.

- [ ] **Schritt 1: `-finished` implementieren**

Zusätzlich zu Entities und Repository:

- `BookService` mit `@Transactional` — eine Methode
  `void raisePrices(BigDecimal factor)`, die alle Preise anhebt, und eine
  Methode `void raisePricesAndFail(BigDecimal factor)`, die nach der
  Änderung absichtlich eine `IllegalStateException` wirft. Zweite Methode
  ist die Rollback-Demo.
- `SeedDataRunner` als `CommandLineRunner`, legt zwei Autoren und fünf
  Bücher an.

`src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:bookstore
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.h2.console.enabled=true
```

- [ ] **Schritt 2: Tests schreiben**

`src/test/java/tech/erben/springboot/basics/data/BookRepositoryTest.java`
mit `@DataJpaTest`:

```java
package tech.erben.springboot.basics.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/test-books.sql")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("findByIsbn findet ein vorhandenes Buch")
    void findsByIsbn() {
        assertThat(bookRepository.findByIsbn("978-0-13-468599-1")).isPresent();
    }

    @Test
    @DisplayName("Derived Query sucht Titelfragmente unabhaengig von Gross-/Kleinschreibung")
    void findsByTitleFragment() {
        assertThat(bookRepository.findByTitleContainingIgnoreCase("java"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Derived Query filtert nach Preisgrenze")
    void findsBelowPriceLimit() {
        assertThat(bookRepository.findByNetPriceLessThan(new BigDecimal("40.00")))
                .hasSize(1);
    }

    @Test
    @DisplayName("JPQL-Query findet Buecher ueber den Autorennamen")
    void findsByAuthorName() {
        assertThat(bookRepository.findByAuthorName("Joshua Bloch")).hasSize(2);
    }
}
```

Dazu `src/test/resources/test-books.sql` mit zwei Autoren und drei Büchern,
so dass die Erwartungen oben aufgehen: zwei Bücher von Joshua Bloch mit
„Java" im Titel, davon eines unter 40 Euro.

- [ ] **Schritt 3: Rollback-Test schreiben**

`BookServiceTransactionTest` mit `@SpringBootTest`: ruft
`raisePricesAndFail` auf, fängt die `IllegalStateException` und prüft, dass
die Preise unverändert sind.

- [ ] **Schritt 4: Tests laufen lassen**

```bash
mvn -B -pl demos/sb-basics-data-jpa-demo/sb-basics-data-jpa-demo-finished -am test
```

Erwartung: `Tests run: 5, Failures: 0, Errors: 0`.

- [ ] **Schritt 5: `-start` anlegen**

Enthält `DataDemoApplication` und leere Package-Struktur, dazu die
`application.properties`. `Book`, `Author`, `BookRepository`, `BookService`
und `SeedDataRunner` fehlen — sie entstehen live. Kein Test.

```bash
mvn -B -pl demos/sb-basics-data-jpa-demo/sb-basics-data-jpa-demo-start -am package
```

- [ ] **Schritt 6: README, POM-Eintrag, Gesamtbuild, Commit**

```bash
git add demos/sb-basics-data-jpa-demo demos/pom.xml
git commit -m "feat(basics): Demo fuer Modul 03 Data und JPA"
```

---

## Task 11: Slides Modul 03 — Data und JPA

**Files:**

- Create: `slides/03_Data_JPA/slides.md`, `images/.gitkeep`

**Interfaces:**

- Consumes: Entities und Repository-Signaturen aus Task 10.

- [ ] **Schritt 1: Deck schreiben**

```text
# Persistenz mit Spring Data JPA
  ## In diesem Modul
  ## Das Problem: Objekte und Tabellen
  ## Was ORM loest — und was nicht
# Entities
  ## @Entity, @Id, @GeneratedValue
  ## Spaltennamen und Typen
  ## @ManyToOne
  ## @OneToMany und mappedBy
  ## FetchType: EAGER und LAZY
  ## Ausblick: das N+1-Problem
# Repositories
  ## Das Repository-Pattern
  ## JpaRepository: was man geschenkt bekommt
  ## Derived Query Methods
  ## Die Namensregeln
  ## Grenzen der Ableitung
  ## @Query mit JPQL
  ## Parameter binden
# Transaktionen
  ## Warum Transaktionen?
  ## @Transactional
  ## Rollback: wann automatisch?
  ## Lesende Transaktionen
  ## Wo gehoert @Transactional hin?
# Datenbank und Konfiguration
  ## H2 fuer die Entwicklung
  ## Die H2-Console
  ## ddl-auto: die vier Werte
  ## Warum ddl-auto nicht in Produktion gehoert
  ## Umstieg auf PostgreSQL
# Demo
  ## Was wir gleich bauen
# Uebung
  ## Assignment 03
```

Die Slide „Warum `ddl-auto` nicht in Produktion gehört" verweist nach vorn
auf `13_Data`, wo Flyway und Liquibase behandelt werden.

- [ ] **Schritt 2: Typnamen prüfen, Frontmatter, Lint, Umfang**

```bash
grep -o "class [A-Za-z]*\|interface [A-Za-z]*" slides/03_Data_JPA/slides.md | sort -u
head -7 slides/03_Data_JPA/slides.md
npx --yes markdownlint-cli2 "slides/03_Data_JPA/slides.md"
wc -l slides/03_Data_JPA/slides.md
```

- [ ] **Schritt 3: Committen**

```bash
git add slides/03_Data_JPA
git commit -m "feat(basics): Slides fuer Modul 03 Data und JPA"
```

---

## Task 12: Assignment und Solution Modul 03

**Files:**

- Create: `assignments/sb-basics-data-jpa-assignment/`
- Create: `solutions/sb-basics-data-jpa-solution/`
- Modify: `assignments/pom.xml`, `solutions/pom.xml`

**Interfaces:**

- Produces:
  - `@Entity class Course` mit `Long id`, `String code`, `String title`,
    `int seats`, `BigDecimal netFee`, `Trainer trainer` (`@ManyToOne`)
  - `@Entity class Trainer` mit `Long id`, `String name`, `String email`
  - `record CourseSummary(String code, String trainerName)`
  - `interface CourseRepository extends JpaRepository<Course, Long>` mit
    `Optional<Course> findByCode(String code)`,
    `List<Course> findByTitleContainingIgnoreCase(String fragment)`,
    `List<Course> findBySeatsGreaterThan(int minimum)`,
    `List<Course> findByFeeRange(BigDecimal min, BigDecimal max)` (`@Query`),
    `List<CourseSummary> findSummaries()` (`@Query` mit `select new`)

**Wichtig zum Zuschnitt:** Der Assignment-Code muss immer kompilieren, sonst
bricht er den Reaktor — Surefire-Skip hilft gegen rote Tests, nicht gegen
Compile-Fehler. Deshalb werden **alle** Typen und Methodensignaturen fertig
ausgeliefert. Rot ist der Ausgangszustand nicht wegen fehlender Typen,
sondern weil der Spring-Kontext nicht hochkommt: `Course` ist noch keine
Entity, und zwei Repository-Methoden lassen sich nicht aus dem Namen
ableiten.

- [ ] **Schritt 1: Assignment anlegen**

Package `tech.erben.springboot.basics.data.task`.

Fertig ausgeliefert und kompilierbar:

| Datei | Zustand |
| --- | --- |
| `CourseAdminApplication` | fertig |
| `Trainer` | fertige Entity mit `@Entity`, `@Id`, `@GeneratedValue` |
| `Course` | **einfache Klasse ohne jede JPA-Annotation**, Felder und Getter/Setter vorhanden |
| `CourseSummary` | fertiges Record |
| `CourseRepository` | Interface mit allen fünf Methodendeklarationen, **ohne** `@Query` an den letzten beiden |
| `src/test/resources/test-courses.sql` | Testdaten: zwei Trainer, vier Kurse |

Drei Aufgaben:

1. `Course` als Entity mappen: `@Entity`, `@Id` mit `@GeneratedValue`,
   `@Column(unique = true)` auf `code`, `@ManyToOne` auf `trainer`
2. `findByFeeRange` mit `@Query` und benannten Parametern (`@Param`)
   implementieren — der Methodenname ist bewusst nicht ableitbar
3. `findSummaries` mit `@Query` und `select new` auf `CourseSummary`
   implementieren

Die drei bereits ableitbaren Methoden (`findByCode`,
`findByTitleContainingIgnoreCase`, `findBySeatsGreaterThan`) bleiben
unangetastet. Sie sind Anschauungsmaterial: Sobald `Course` eine Entity ist,
funktionieren sie ohne eine Zeile Code. Die README weist ausdrücklich darauf
hin — das ist der Aha-Moment des Moduls.

- [ ] **Schritt 2: Tests schreiben**

`CourseRepositoryTest` mit `@DataJpaTest` und `@Sql("/test-courses.sql")`,
fünf Tests, Struktur analog zu `BookRepositoryTest` aus Task 10. Die
`@DisplayName`-Texte ordnen zu: drei Tests decken die abgeleiteten Methoden
ab (`"Ohne Zutun: …"`), zwei die Aufgaben (`"Aufgabe 2: …"`,
`"Aufgabe 3: …"`).

- [ ] **Schritt 3: Prüfen, dass alles kompiliert**

```bash
mvn -B -pl assignments/sb-basics-data-jpa-assignment -am test-compile
```

Erwartung: `BUILD SUCCESS`. Schlägt das fehl, ist eine Signatur unvollständig
ausgeliefert — nachbessern, bevor es weitergeht.

- [ ] **Schritt 4: Tests müssen rot sein**

```bash
mvn -B -pl assignments/sb-basics-data-jpa-assignment -am test -DskipAssignmentTests=false
```

Erwartung: **FAIL**, alle fünf Tests, mit
`IllegalArgumentException: Not a managed type: class …Course` beim
Hochfahren des Kontexts. Nach Aufgabe 1 verschiebt sich der Fehler auf die
beiden nicht ableitbaren Methoden
(`PropertyReferenceException: No property 'feeRange' found`) — dieser
Zwischenzustand steht als Hinweis in der README, damit ihn niemand für
einen eigenen Fehler hält.

- [ ] **Schritt 5: README nach Template**

Drei Aufgaben, Bonusaufgabe (`@Query` mit Aggregat: durchschnittliche
Netto-Gebühr je Trainer), Erfolgskriterien, Verweis auf
`solutions/sb-basics-data-jpa-solution`. Der Abschnitt „Was liegt bereit?"
nennt explizit, dass drei Repository-Methoden bereits funktionieren, sobald
Aufgabe 1 erledigt ist.

- [ ] **Schritt 6: Solution anlegen und grün prüfen**

Kopie wie in Task 4 Schritt 5, `Course` gemappt, beide `@Query`-Annotationen
ergänzt, Property `skipAssignmentTests` **entfernt**.

```bash
mvn -B -pl solutions/sb-basics-data-jpa-solution -am test
```

Erwartung: `Tests run: 5, Failures: 0, Errors: 0`.

- [ ] **Schritt 7: Registrieren, Gesamtbuild, Commit**

```bash
mvn -B verify
git add assignments/sb-basics-data-jpa-assignment solutions/sb-basics-data-jpa-solution \
        assignments/pom.xml solutions/pom.xml
git commit -m "feat(basics): Uebung und Loesung fuer Modul 03 Data und JPA"
```

---

## Task 13: Demo Modul 04 — Testing

**Files:**

- Create: `demos/sb-basics-testing-demo/pom.xml` (Aggregator)
- Create: `demos/sb-basics-testing-demo/sb-basics-testing-demo-start/`
- Create: `demos/sb-basics-testing-demo/sb-basics-testing-demo-finished/`
- Create: `demos/sb-basics-testing-demo/README.md`
- Modify: `demos/pom.xml`

**Interfaces:**

- Consumes: Den REST-Vertrag aus Task 7 und das JPA-Modell aus Task 10 —
  die Testing-Demo ist eine kombinierte Buchhandlung mit Controller,
  Service und Repository, damit alle drei Testarten etwas zu testen haben.

**Dependencies:** `spring-boot-starter-web`,
`spring-boot-starter-data-jpa`, `spring-boot-starter-validation`,
`com.h2database:h2` (`runtime`), `spring-boot-starter-test`.

- [ ] **Schritt 1: Produktivcode in `-start` und `-finished` identisch anlegen**

Beide Module enthalten denselben, vollständigen Produktivcode:
`Book`-Entity, `BookRepository`, `BookService` (mit einer Rabattregel:
ab fünf Exemplaren 10 % Nachlass), `BookController`, `RestExceptionHandler`.

Unterschied ist ausschließlich `src/test/java`: `-start` hat es leer,
`-finished` enthält die drei Testklassen.

- [ ] **Schritt 2: Unit-Test mit Mockito schreiben (`-finished`)**

`BookServiceTest` ohne Spring-Kontext:

```java
package tech.erben.springboot.basics.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    @DisplayName("Ab fuenf Exemplaren gibt es zehn Prozent Rabatt")
    void appliesBulkDiscount() {
        Book book = new Book();
        book.setIsbn("978-0-13-468599-1");
        book.setNetPrice(new BigDecimal("100.00"));
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(book));

        BigDecimal total = bookService.totalFor("978-0-13-468599-1", 5);

        assertThat(total).isEqualByComparingTo(new BigDecimal("450.00"));
    }

    @Test
    @DisplayName("Unter fuenf Exemplaren gibt es keinen Rabatt")
    void appliesNoDiscountBelowThreshold() {
        Book book = new Book();
        book.setIsbn("978-0-13-468599-1");
        book.setNetPrice(new BigDecimal("100.00"));
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(book));

        BigDecimal total = bookService.totalFor("978-0-13-468599-1", 4);

        assertThat(total).isEqualByComparingTo(new BigDecimal("400.00"));
        verify(bookRepository, times(1)).findByIsbn("978-0-13-468599-1");
    }
}
```

Daraus die Signatur: `BigDecimal totalFor(String isbn, int quantity)`.

- [ ] **Schritt 3: Slice-Test mit `@WebMvcTest` schreiben**

`BookControllerWebMvcTest` mit `@WebMvcTest(BookController.class)` und
`@MockitoBean BookService bookService` — **nicht** `@MockBean`, das gibt es
in Spring Boot 4 nicht mehr.

- [ ] **Schritt 4: Integrationstest mit `@SpringBootTest` schreiben**

`BookstoreIntegrationTest` mit
`@SpringBootTest(webEnvironment = RANDOM_PORT)` und `TestRestTemplate`,
der einen echten POST absetzt und danach per GET nachliest.

- [ ] **Schritt 5: Tests laufen lassen**

```bash
mvn -B -pl demos/sb-basics-testing-demo/sb-basics-testing-demo-finished -am test
```

Erwartung: `Tests run: 6, Failures: 0, Errors: 0` (2 Unit, 2 Slice, 2
Integration).

- [ ] **Schritt 6: `-start` prüfen**

```bash
mvn -B -pl demos/sb-basics-testing-demo/sb-basics-testing-demo-start -am test
```

Erwartung: `BUILD SUCCESS`, `Tests run: 0`. Das leere Testverzeichnis ist
Absicht — hier wird live geschrieben.

- [ ] **Schritt 7: README, POM-Eintrag, Gesamtbuild, Commit**

```bash
git add demos/sb-basics-testing-demo demos/pom.xml
git commit -m "feat(basics): Demo fuer Modul 04 Testing"
```

---

## Task 14: Slides Modul 04 — Testing

**Files:**

- Create: `slides/04_Testing/slides.md`, `images/.gitkeep`

**Interfaces:**

- Consumes: Testklassen und Signaturen aus Task 13.

- [ ] **Schritt 1: Deck schreiben**

Kürzer als die anderen — 1,5 Unterrichtsstunden, also 300–450 Zeilen:

```text
# Testen mit JUnit 5
  ## In diesem Modul
  ## Warum automatisiert testen?
  ## Die Testpyramide in einem Satz
# JUnit 5 Grundlagen
  ## @Test und der erste Test
  ## Assertions
  ## AssertJ: fluent assertions
  ## @BeforeEach und @AfterEach
  ## @DisplayName
  ## Erwartete Exceptions
# Unit-Tests mit Mockito
  ## Warum Mocks?
  ## @Mock und @InjectMocks
  ## when und thenReturn
  ## verify
  ## Wann NICHT mocken
# Spring-Tests
  ## @SpringBootTest: der volle Kontext
  ## Was das kostet
  ## Test Slices
  ## @WebMvcTest und MockMvc
  ## @MockitoBean
  ## @DataJpaTest
  ## Welchen Test wann?
# Demo
  ## Was wir gleich schreiben
# Uebung
  ## Assignment 04
```

Die Slide „`@MockitoBean`" weist ausdrücklich darauf hin, dass ältere
Beispiele im Netz `@MockBean` zeigen und dass diese Annotation seit Spring
Boot 3.4 abgelöst und in Boot 4 entfernt ist. Ohne diesen Hinweis
kopieren Teilnehmer Code, der nicht kompiliert.

- [ ] **Schritt 2: Typnamen prüfen, Frontmatter, Lint, Umfang**

```bash
grep -o "class [A-Za-z]*" slides/04_Testing/slides.md | sort -u
head -7 slides/04_Testing/slides.md
npx --yes markdownlint-cli2 "slides/04_Testing/slides.md"
wc -l slides/04_Testing/slides.md
```

Erwartung: 300–450 Zeilen.

- [ ] **Schritt 3: Committen**

```bash
git add slides/04_Testing
git commit -m "feat(basics): Slides fuer Modul 04 Testing"
```

---

## Task 15: Assignment und Solution Modul 04

Umgekehrter Vertrag: Der Produktivcode ist fertig, die Teilnehmer schreiben
die Tests.

**Files:**

- Create: `assignments/sb-basics-testing-assignment/`
- Create: `solutions/sb-basics-testing-solution/`
- Modify: `assignments/pom.xml`, `solutions/pom.xml`

**Interfaces:**

- Consumes: `Course`, `CourseRepository` aus Task 12, den REST-Vertrag aus
  Task 9.
- Produces: `class ParticipantService` mit
  `boolean register(String courseCode, String email)` — meldet an, solange
  freie Plätze da sind, und gibt `false` zurück, wenn der Kurs voll ist.

- [ ] **Schritt 1: Assignment anlegen**

Package `tech.erben.springboot.basics.testing.task`. Vollständig fertig
geliefert: `CourseAdminApplication`, `Course`, `Participant`,
`CourseRepository`, `ParticipantRepository`, `ParticipantService`,
`ParticipantController`, `RestExceptionHandler`. `src/test/java` ist leer.

Drei Aufgaben:

1. Unit-Test für `ParticipantService.register` mit `@Mock` und
   `@InjectMocks`: einmal freie Plätze, einmal ausgebucht
2. `@WebMvcTest` für `ParticipantController` mit `@MockitoBean` auf
   `ParticipantService`: `201` bei Erfolg, `409` bei ausgebuchtem Kurs
3. `@SpringBootTest` mit `TestRestTemplate`: Anmeldung end-to-end

- [ ] **Schritt 2: README mit Mutationskriterium schreiben**

Der Abschnitt „Erfolgskriterien" enthält den Mutationstest wörtlich:

```markdown
## Erfolgskriterien

- [ ] `mvn test` laeuft gruen und fuehrt mindestens fuenf Tests aus.
- [ ] Mutationsprobe: Aendere in `ParticipantService` Zeile 34 die
      Bedingung `registered < course.getSeats()` zu
      `registered <= course.getSeats()`. Mindestens einer deiner Tests
      muss jetzt rot werden. Mache die Aenderung danach rueckgaengig.
- [ ] Mutationsprobe: Aendere in `ParticipantController` den Statuscode
      fuer den Konfliktfall von `409` auf `400`. Mindestens einer deiner
      Tests muss jetzt rot werden. Mache die Aenderung danach rueckgaengig.
```

Die genannten Zeilennummern müssen beim Schreiben gegen die tatsächliche
Datei geprüft und angepasst werden.

- [ ] **Schritt 3: Solution mit vollständigen Tests anlegen**

Kopie des Assignments plus fünf Testklassen-Methoden, die beide
Mutationsproben bestehen.

- [ ] **Schritt 4: Solution grün prüfen**

```bash
mvn -B -pl solutions/sb-basics-testing-solution -am test
```

Erwartung: `Tests run: 5, Failures: 0, Errors: 0`.

- [ ] **Schritt 5: Beide Mutationsproben in der Solution verifizieren**

Für jede Mutation: Änderung anwenden, `mvn -B -pl
solutions/sb-basics-testing-solution test` laufen lassen, **FAIL**
erwarten, Änderung zurücknehmen.

```bash
mvn -B -pl solutions/sb-basics-testing-solution -am test
```

Erwartung nach dem Zurücknehmen: wieder grün. Besteht eine Mutation den
Test, decken die Solution-Tests die Regel nicht ab — dann fehlt ein Test.

- [ ] **Schritt 6: Registrieren, Gesamtbuild, Commit**

Das Assignment hat ein leeres `src/test/java` und braucht daher **keinen**
Surefire-Skip.

```bash
mvn -B verify
git add assignments/sb-basics-testing-assignment solutions/sb-basics-testing-solution \
        assignments/pom.xml solutions/pom.xml
git commit -m "feat(basics): Uebung und Loesung fuer Modul 04 Testing"
```

---

## Task 16: Demo Modul 05 — Betrieb

**Files:**

- Create: `demos/sb-basics-operations-demo/pom.xml`
- Create: `demos/sb-basics-operations-demo/src/main/java/tech/erben/springboot/basics/operations/`
- Create: `demos/sb-basics-operations-demo/src/main/resources/application.properties`
- Create: `demos/sb-basics-operations-demo/Dockerfile`
- Create: `demos/sb-basics-operations-demo/README.md`
- Modify: `demos/pom.xml`

**Dependencies:** `spring-boot-starter-web`,
`spring-boot-starter-actuator`, `spring-boot-starter-test`.

- [ ] **Schritt 1: Modul anlegen**

Klassen unter `tech.erben.springboot.basics.operations`:

| Klasse | Zweck |
| --- | --- |
| `OperationsDemoApplication` | Einstiegspunkt |
| `OrderController` | `GET /api/orders`, loggt auf `info` und `debug` |
| `OrderService` | erzeugt Beispielbestellungen, loggt auf `debug` |

`application.properties`:

```properties
spring.application.name=bookstore
management.endpoints.web.exposure.include=health,info,metrics,loggers
management.endpoint.health.show-details=always
info.app.name=Buchhandlung Erben
logging.level.tech.erben=INFO
```

- [ ] **Schritt 2: Dockerfile schreiben**

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/sb-basics-operations-demo-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Schritt 3: Test schreiben**

`ActuatorEndpointsTest` mit
`@SpringBootTest(webEnvironment = RANDOM_PORT)` und `TestRestTemplate`:
prüft, dass `/actuator/health` mit `200` und `{"status":"UP"}` antwortet und
`/actuator/info` den konfigurierten App-Namen enthält.

- [ ] **Schritt 4: Test laufen lassen**

```bash
mvn -B -pl demos/sb-basics-operations-demo -am test
```

Erwartung: `Tests run: 2, Failures: 0, Errors: 0`.

- [ ] **Schritt 5: Log-Level zur Laufzeit ändern**

```bash
mvn -B -pl demos/sb-basics-operations-demo -am package
java -jar demos/sb-basics-operations-demo/target/sb-basics-operations-demo-1.0.0-SNAPSHOT.jar &
sleep 12
curl -s localhost:8080/api/orders > /dev/null
curl -s -X POST localhost:8080/actuator/loggers/tech.erben \
     -H "Content-Type: application/json" \
     -d '{"configuredLevel":"DEBUG"}'
curl -s localhost:8080/api/orders > /dev/null
kill %1
```

Erwartung: Nach dem POST erscheinen die `debug`-Zeilen aus `OrderService`
in der Konsole, vorher nicht. Das ist die Demo-Pointe.

- [ ] **Schritt 6: Docker-Image bauen**

```bash
docker build -t sb-basics-operations-demo demos/sb-basics-operations-demo
```

Erwartung: erfolgreicher Build. Ist Docker lokal nicht verfügbar, den
Schritt überspringen und im Ergebnis explizit vermerken — nicht stillschweigend
weglassen.

- [ ] **Schritt 7: README, POM-Eintrag, Gesamtbuild, Commit**

```bash
git add demos/sb-basics-operations-demo demos/pom.xml
git commit -m "feat(basics): Demo fuer Modul 05 Betrieb"
```

---

## Task 17: Slides Modul 05 — Betrieb

**Files:**

- Create: `slides/05_Betrieb/slides.md`, `images/.gitkeep`

**Interfaces:**

- Consumes: `OrderController`, `OrderService`, `application.properties` und
  das Dockerfile aus Task 16.

- [ ] **Schritt 1: Deck schreiben**

Kürzestes Deck — eine Unterrichtsstunde, 200–320 Zeilen:

```text
# Betrieb: Von der IDE in den Container
  ## In diesem Modul
# Logging
  ## SLF4J und Logback
  ## Einen Logger holen
  ## Die Log-Level
  ## Level konfigurieren
  ## Was gehoert ins Log — und was nicht
# Actuator
  ## Was Actuator liefert
  ## Einbinden und freischalten
  ## /actuator/health
  ## /actuator/info und /actuator/metrics
  ## Log-Level zur Laufzeit aendern
  ## Ausblick: eigene Health Indicators
# Konfiguration von aussen
  ## Warum nichts ins Image gehoert
  ## Environment-Variablen
  ## Die Namensregel: SHOP_PAGE_SIZE
  ## Kommandozeilenargumente
# Container
  ## Das Fat-JAR
  ## Ein einfaches Dockerfile
  ## spring-boot:build-image
  ## Was jetzt fehlt — Ausblick auf den Advanced-Teil
```

Die Abschluss-Slide „Was jetzt fehlt" zählt auf, was der Advanced-Teil
bringt: eigene Metriken, Tracing, Security, Messaging, Resilience. Sie ist
die Brücke zwischen den beiden Blöcken.

- [ ] **Schritt 2: Frontmatter, Lint, Umfang**

```bash
head -7 slides/05_Betrieb/slides.md
npx --yes markdownlint-cli2 "slides/05_Betrieb/slides.md"
wc -l slides/05_Betrieb/slides.md
```

Erwartung: 200–320 Zeilen.

- [ ] **Schritt 3: Committen**

```bash
git add slides/05_Betrieb
git commit -m "feat(basics): Slides fuer Modul 05 Betrieb"
```

---

## Task 18: Gesamtintegration

Läuft im Orchestrator, nicht in einem Subagent.

**Files:**

- Create: `README.md` im Repository-Wurzelverzeichnis
- Modify: `AGENTS.md` — Struktur-Abschnitt um die Zweiteilung ergänzen

- [ ] **Schritt 1: Vollständigkeit prüfen**

```bash
ls slides/
```

Erwartung: `00_Spring_Core`, `01_Spring_Boot_Basics`, `02_Web_REST`,
`03_Data_JPA`, `04_Testing`, `05_Betrieb`,
`10_Microservices_Architecture`, `11_Configuration`, `12_Testing`,
`13_Data`, `14_Web`, `15_Actuator`, `16_Security`, `17_Messaging`,
`template.html`. Genau 14 Modulverzeichnisse.

```bash
grep -h "^header:" slides/*/slides.md | sort | uniq -c
```

Erwartung: `6 header: Spring Boot Basics` und
`8 header: Spring Boot Advanced`.

- [ ] **Schritt 2: Vollbuild mit Tests**

```bash
mvn -B verify
```

Erwartung: `BUILD SUCCESS`. Die vier Assignments überspringen ihre Tests
(bzw. haben keine), alle Solutions und Demos laufen grün.

Voraussetzung: Der in Task 1 Schritt 1 beschriebene rote
`OAuth2DemoControllerTest` ist behoben. Ist er es nicht, endet dieser
Schritt zwangsläufig rot — dann auf die Basis-Module eingrenzen (Kommando
unten) und den offenen Bestandsfehler im Abschlussbericht nennen, statt ihn
zu übergehen.

Achtung, Laufzeit: Der Advanced-Teil enthält Testcontainers-Tests, die
Docker-Images ziehen (unter anderem `rabbitmq:3.13-management`). Der erste
Lauf dauert entsprechend lange und braucht ein laufendes Docker. Wer nur
die Basis-Module prüfen will:

```bash
mvn -B verify -pl "$(ls -d demos/sb-basics-* assignments/sb-basics-* \
    solutions/sb-basics-* | paste -sd, -)" -am
```

- [ ] **Schritt 3: Assignments einzeln gegen ihre Solutions prüfen**

Für jedes der vier Paare: Assignment-Tests mit
`-DskipAssignmentTests=false` rot, Solution-Tests grün.

```bash
for m in core web data-jpa; do
  echo "=== $m Assignment (erwartet: FAIL) ==="
  mvn -B -q -pl assignments/sb-basics-$m-assignment test \
      -DskipAssignmentTests=false 2>&1 | tail -5
  echo "=== $m Solution (erwartet: PASS) ==="
  mvn -B -q -pl solutions/sb-basics-$m-solution test 2>&1 | tail -5
done
```

Das Testing-Paar hat ein leeres Assignment-Testverzeichnis und wird
separat geprüft (Task 15, Schritt 5).

- [ ] **Schritt 4: Linter über alles**

```bash
npx --yes markdownlint-cli2 "**/*.md" "#**/target/**" "#**/node_modules/**"
```

Erwartung: `Summary: 0 issues`.

Link-Check über die neuen Decks und READMEs — die Slides verweisen auf die
Spring-Dokumentation, tote Links fallen sonst erst in der CI auf:

```bash
lychee --accept 429,200 --exclude 'http://localhost.*' --max-concurrency 4 \
       --retry-wait-time 2 --timeout 20 --cache \
       slides/0*/slides.md demos/sb-basics-*/README.md \
       assignments/sb-basics-*/README.md README.md
```

Erwartung: keine `ERROR`-Zeilen.

- [ ] **Schritt 5: Repository-README schreiben**

`README.md` mit: Kursüberblick, Zweiteilung Basis (2 Tage) und Advanced
(3 Tage), Modultabelle mit Zuordnung Slide-Verzeichnis → Demo →
Assignment, Voraussetzungen (Java 21, Maven 3.9, Docker optional),
Startanleitung (`mvn verify`).

- [ ] **Schritt 6: `AGENTS.md` ergänzen**

Im Abschnitt „Struktur" ergänzen: Die Slides teilen sich in Basis
(`00_`–`05_`, Header „Spring Boot Basics") und Advanced (`10_`–`17_`,
Header „Spring Boot Advanced"). Neue Basis-Module tragen das Präfix
`sb-basics-`, Advanced-Module `sb-advanced-`.

- [ ] **Schritt 7: Pre-Commit über den Gesamtstand**

```bash
pre-commit run --all-files
```

Erwartung: alle Hooks `Passed`.

- [ ] **Schritt 8: Committen**

```bash
git add README.md AGENTS.md
git commit -m "docs: Kursuebersicht fuer Basis- und Advanced-Teil"
```

- [ ] **Schritt 9: GitLab-Repository umbenennen — nur nach Rückfrage**

Nicht automatisch ausführen. Dem Nutzer vorlegen:

```bash
glab repo update it-erben/gfu/spring-boot-advanced \
     --name "spring-boot" --path "spring-boot"
git remote set-url origin git@gitlab.com:it-erben/gfu/spring-boot.git
```

Das lokale Verzeichnis heißt danach weiterhin `spring-boot-advanced`; ob es
mitumbenannt wird, entscheidet der Nutzer.

---

## Offene Punkte für die Ausführung

- **Zeilennummern in Task 15** (Mutationsproben) stehen erst fest, wenn
  `ParticipantService` und `ParticipantController` geschrieben sind. Beim
  Schreiben der README gegen die tatsächlichen Dateien prüfen.
- **Docker in Task 16** ist optional. Fehlt es lokal, wird Schritt 6
  übersprungen und das im Ergebnis vermerkt.
- **Bilder:** Keines der Basis-Decks braucht zwingend Diagramme. Wo eine
  Skizze hilft (Container/Bean-Lebenszyklus in Modul 00, Filter-Kette gibt
  es hier noch nicht), kann nachträglich ein `drawio.svg` ergänzt werden;
  die `images/`-Verzeichnisse sind mit `.gitkeep` vorbereitet.
