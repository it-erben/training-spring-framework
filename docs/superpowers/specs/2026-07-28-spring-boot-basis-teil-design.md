# Basis-Teil für die Spring-Boot-Schulung

## Ziel

Der Kurs deckt bisher nur Fortgeschrittenenthemen ab. Die Advanced-Slides
setzen Grundlagen voraus, ohne sie zu liefern — mehrere Module beginnen mit
"Wiederholung:"-Slides, die auf nichts zurückverweisen. Dieses Dokument
beschreibt einen zweitägigen Basis-Teil, der diese Lücke schließt und im
selben Repository dem Advanced-Teil vorangestellt wird.

## Rahmenbedingungen

- **Zielgruppe:** Java-Entwickler ohne Spring-Kenntnisse. Java 21 sitzt,
  Dependency Injection als Konzept ist neu, Maven-Erfahrung nicht
  vorausgesetzt.
- **Dauer:** 2 Tage, netto rund 12 Stunden inklusive Übungen.
- **Verhältnis zum Advanced-Teil:** ein Repository, zwei Blöcke. Basis (2
  Tage) und Advanced (3 Tage) sind getrennt oder zusammenhängend buchbar.
- **Sprache und Format:** deutsch, Marp-Slides wie im Bestand.

## Struktur und Umbenennung

Mit einem Basis-Teil trägt das Repository einen falschen Namen. Die
Umbenennung ist billig: Der Kurs wird nicht über Flux deployt — in
`proxmox-setup/clusters/k8s-01/apps/` und `image-automation/` existiert kein
Manifest, und die `.gitlab-ci.yml` bindet keine `training-deploy`-Komponente
ein, nur PDF-Publisher, Linter, Maven-Verify und Release. Es sind also keine
Flux-Manifeste, keine Subdomain und kein cert-manager betroffen.

### Repository und Maven-Koordinaten

- GitLab-Repo `gfu/spring-boot-advanced` → `gfu/spring-boot`. GitLab legt
  beim Rename eine Weiterleitung an, bestehende Clones brechen nicht sofort.
  Lokales Verzeichnis analog, danach `git remote set-url`.
- Root-POM `tech.erben:sb-advanced` → `tech.erben:sb-training`.
- Aggregatoren: `sb-advanced-assignments` → `sb-training-assignments`, analog
  für `solutions` und `demos`. Verzeichnisnamen bleiben.
- Die bestehenden 13 Demo- und 7 Assignment/Solution-Module behalten ihre
  Namen (`sb-advanced-*`). Sie gehören inhaltlich weiter zum Advanced-Block,
  und ein Massen-Rename von 20 Verzeichnissen bringt nur Merge-Konflikte in
  offenen Branches.
- Neue Basis-Module heißen `sb-basics-*`.

### Slide-Verzeichnisse

```text
slides/00_Spring_Core/slides.md          (neu)
slides/01_Spring_Boot_Basics/slides.md   (neu)
slides/02_Web_REST/slides.md             (neu)
slides/03_Data_JPA/slides.md             (neu)
slides/04_Testing/slides.md              (neu)
slides/05_Betrieb/slides.md              (neu)
slides/10_Microservices_Architecture/    (war 00_)
slides/11_Configuration/                 (war 01_)
slides/12_Testing/                       (war 02_)
slides/13_Data/                          (war 03_)
slides/14_Web/                           (war 04_)
slides/15_Actuator/                      (war 05_)
slides/16_Security/                      (war 06_)
slides/17_Messaging/                     (war 07_)
```

Umbenennung per `git mv`, damit die Historie erhalten bleibt. Die
`images/`-Unterverzeichnisse ziehen mit. Bildverweise in den Slides sind
relativ (`images/…`); absolute Pfade oder Verweise auf Verzeichnisnamen
existieren im Repository nicht.

### Titel und Kopfzeilen

- Basis-Module: `header: Spring Boot Basics`
- Advanced-Module: `header: Spring Boot Advanced` (unverändert)
- Footer bleibt überall `Alexander Erben`.
- `slides/template.html`: `<title>` und `<h1>` werden "Spring Boot Schulung".
  Die Linkliste umfasst dann beide Blöcke.

## Module

Umfang je Modul rund 400–600 Zeilen Marp, im Stil des Bestands:
Bullet-Slides mit Code-Blöcken, durch `---` getrennt.

### `00_Spring_Core` (2,5 h)

Warum überhaupt ein Container? Inversion of Control und Dependency Injection
als Konzept, bevor die erste Annotation fällt. Dann `@Component`, `@Service`,
`@Repository`, Component Scan, Konstruktor-Injection als Standard und warum
nicht Feld-Injection, `@Configuration` mit `@Bean` für Fremdklassen,
`@Qualifier` und `@Primary` bei Mehrdeutigkeit, Bean Scopes (Singleton und
Prototype ausführlich, Request und Session nur benannt),
Lifecycle-Callbacks, `@Value`.

Maven-Grundlagen laufen nebenher: Aufbau einer `pom.xml`, Parent-POM,
Aufnahme einer Dependency.

Brücke: `11_Configuration` steigt direkt mit AutoConfiguration ein und setzt
genau das voraus.

### `01_Spring_Boot_Basics` (2 h)

Was Boot über Spring drauflegt. Starter-Konzept,
`spring-boot-starter-parent`, Autoconfiguration konzeptionell ("Was auf dem
Classpath liegt, wird konfiguriert" — die `@Conditional`-Interna bleiben
`11_Configuration` vorbehalten), `@SpringBootApplication` auseinandergenommen,
`application.properties` und `.yaml`, Profile in einfacher Form,
`spring.config`-Grundlagen, DevTools, Projektstruktur, Spring Initializr,
Fat-JAR und `spring-boot:run`.

Brücke: `11_Configuration` vertieft Precedence, `@ConstructorBinding` und
eigene Starter.

### `02_Web_REST` (2,5 h)

`@RestController`, `@GetMapping` und Verwandte, `@PathVariable`,
`@RequestParam`, `@RequestBody`, `ResponseEntity` und Statuscodes,
Jackson-Serialisierung mit Records, `@ExceptionHandler` und
`@ControllerAdvice` in einfacher Form, Bean Validation mit `@Valid`,
`@NotNull` und `@Size`, ein Blick auf `RestClient` als Aufrufer.

Brücke: `14_Web` beginnt mit "Wiederholung: Der `@RestController`" und
"Wiederholung: `ResponseEntity`" — diese Slides verweisen danach tatsächlich
zurück.

### `03_Data_JPA` (2,5 h)

Warum ORM, `@Entity`, `@Id`, `@GeneratedValue`, Basis-Relationen
(`@OneToMany`, `@ManyToOne`, `FetchType` als Ausblick), `JpaRepository` und
sein Funktionsumfang, Derived Query Methods, `@Query` mit JPQL in einfacher
Form, `@Transactional` als Konzept (Commit und Rollback, keine Propagation),
H2 für die Entwicklung, Datasource-Konfiguration gegen Postgres.

Brücke: `13_Data` beginnt mit JPA-Architektur, Entity Manager und
N+1-Problem — durchgehend Vertiefung.

### `04_Testing` (1,5 h)

JUnit 5 im Kern (`@Test`, Assertions, `@BeforeEach`, `@DisplayName`),
Unit-Test einer Service-Klasse ohne Spring, dann `@SpringBootTest` und was es
kostet, `@WebMvcTest` mit `MockMvc` als erster Test-Slice, Mockito-Basics
(`mock`, `when`, `verify`).

Brücke: `12_Testing` setzt bei parametrisierten Tests, Extensions und
Testcontainers an.

### `05_Betrieb` (1 h)

Logging mit SLF4J und Log-Level-Konfiguration, Actuator einbinden und
`/health` sowie `/info` ansehen, Konfiguration von außen
(Environment-Variablen, `--server.port`), Dockerfile beziehungsweise
`spring-boot:build-image`.

Brücke: `15_Actuator` baut auf dem `/health`-Blick auf, `11_Configuration`
auf der externen Konfiguration.

### Bewusst ausgeschlossen

Security (vollständig in `16`), Messaging (`17`), Reaktives, Spring Cloud,
Caching, Scheduling. Sechs Module in zwei Tagen vertragen keine Extras.

## Demos

Sechs Demos, eine pro Modul, unter `demos/`:

| Modul | Verzeichnis | Aufbau |
| --- | --- | --- |
| `00` | `sb-basics-core-demo` | start + finished |
| `01` | `sb-basics-boot-demo` | nur finished |
| `02` | `sb-basics-web-demo` | start + finished |
| `03` | `sb-basics-data-jpa-demo` | start + finished |
| `04` | `sb-basics-testing-demo` | start + finished |
| `05` | `sb-basics-operations-demo` | nur finished |

Start/Finished-Paare dort, wo live mitprogrammiert wird — so hält es der
Bestand bereits (`sb-advanced-web-demo`, `sb-advanced-testing-demo`). Bei
`01_Spring_Boot_Basics` und `05_Betrieb` wird eher konfiguriert als
programmiert; dort reicht ein fertiges Projekt.

**Gemeinsame Domäne aller Basis-Demos: eine Buchhandlung** (`Book`, `Author`,
`Order`). Die Demos bleiben technisch unabhängig — jede ist ein
eigenständiges Maven-Modul, das für sich startet. Die Teilnehmer müssen sich
aber nicht sechsmal in eine neue Fachlichkeit eindenken, und wer ein Modul
verpasst, kann das nächste trotzdem mitmachen.

## Assignments und Lösungen

Vier Assignments unter `assignments/`, vier Lösungen unter `solutions/`:

| Assignment | Modul | Inhalt |
| --- | --- | --- |
| `sb-basics-core-assignment` | `00` | Vorgegebene Klassen ohne Spring-Annotationen verdrahten: Komponenten deklarieren, Konstruktor-Injection, Fremdklasse per `@Bean` bereitstellen, Mehrdeutigkeit mit `@Qualifier` auflösen |
| `sb-basics-web-assignment` | `02` | CRUD-Endpunkte für eine bestehende Service-Schicht bauen, korrekte Statuscodes, Validierung, `@ControllerAdvice` für 404 |
| `sb-basics-data-jpa-assignment` | `03` | Entity und Repository anlegen, drei Derived Queries, eine `@Query`, Testdaten laden |
| `sb-basics-testing-assignment` | `04` | Zu vorhandenem Code Tests schreiben: Unit-Test mit Mockito, `@WebMvcTest` mit MockMvc, ein `@SpringBootTest` |

Keine Aufgaben für `01_Spring_Boot_Basics` und `05_Betrieb`: Beide sind zu
kurz getaktet, und ihr Stoff ist Konfiguration, die sich in der Demo besser
zeigen als üben lässt.

**Fachlichkeit der Aufgaben: Kursverwaltung** (`Course`, `Participant`,
`Trainer`) — bewusst eine andere Domäne als die Demos, damit die
Demo-Lösung nicht durchkopiert werden kann.

Jedes Assignment folgt `gfu/ASSIGNMENT_README_TEMPLATE.md`: Lernziele,
Voraussetzungen, "Was liegt bereit?", nummerierte Aufgaben, Bonusaufgabe,
Erfolgskriterien als Checkliste, Verweis auf den Lösungsordner.

**Erfolgskriterium ist immer ein Testlauf, nie eine Sichtprüfung.** Die
Assignments `00`, `02` und `03` bringen Tests mit, die zu Beginn rot sind
und nach vollständiger Bearbeitung grün. Das Testing-Assignment dreht das
um: Dort ist der Produktivcode fertig und die Teilnehmer schreiben die
Tests. Erfolgskriterium ist eine in der README benannte Mutation ("Ändere
Zeile X von `>=` zu `>`, dein Test muss rot werden").

Solutions sind vollständig lauffähig. Laut `AGENTS.md` müssen sie synchron
zu den Assignments bleiben; die Aufgaben-README benennt daher explizit den
zugehörigen Lösungsordner.

**Zeitbudget:** vier Aufgaben à 30–40 Minuten sind rund 2,5 h von 12 h netto,
also knapp ein Fünftel Übungsanteil.

## Build-Integration

Die neuen Module hängen sich in die bestehenden Aggregatoren: sechs Einträge
in `demos/pom.xml`, vier in `assignments/pom.xml`, vier in
`solutions/pom.xml`. Das Root-POM ändert sich nur an den Koordinaten.

Spring Boot 4.0.2, Java 21 und Encoding kommen unverändert vom Root-POM.
Basis- und Advanced-Module teilen dieselben Properties und laufen nicht
auseinander.

Package-Basis der neuen Module: `tech.erben.springboot.basics.<modul>`, also
etwa `tech.erben.springboot.basics.web`. Der Bestand ist hier uneinheitlich
(`tech.erben.security`, `tech.erben.springboot.datajpa`, `tech.erben`); für
die neuen Module gilt ein konsistentes Schema, der Bestand bleibt
unangetastet.

## Verifikation

Drei Ebenen, alle bereits in der CI vorhanden:

- **`components/maven/verify`** baut alle Module und führt alle Tests aus.
  Ein Assignment gilt erst als fertig, wenn es ohne die Lösung kompiliert
  und seine Tests rot laufen; die zugehörige Solution muss grün sein. Das
  wird pro Modul lokal geprüft, nicht erst in der CI.
- **`markdown-lint`** über `markdownlint-cli2`. Die in `AGENTS.md` genannte
  Zeilenlänge 80 ist in `.markdownlint.json` per `MD013: false`
  ausgeschaltet und wird nicht erzwungen; neue Slides halten sie trotzdem
  ein, weil der Bestand es tut. Aktiv sind unter anderem `MD060`
  (Tabellen-Pipes mit Leerzeichen) — darauf ist beim Schreiben zu achten.
- **`lychee-lint`** prüft Links. Verweise auf die Spring-Dokumentation in
  neuen Slides müssen erreichbar sein.

Der Pre-Commit-Hook läuft lokal und ist die erste Verteidigungslinie.

### Risiko der Umbenennung

Der `git mv` der acht Advanced-Verzeichnisse ist der einzige Schritt, der
Bestehendes anfasst. Danach wird geprüft: Bilder werden aufgelöst, der
PDF-Publisher findet alle Decks, `mvn verify` läuft durch. Der Schritt ist
ein eigener Commit vor allem Neuen — bei Problemen isoliert revertierbar.

## Umsetzungsreihenfolge

1. **Umbenennung:** `git mv` der Slide-Verzeichnisse, Maven-Koordinaten,
   `template.html`, Marp-Header. Kein neuer Inhalt.
2. **`00_Spring_Core`:** Slides, Demo (start/finished), Assignment, Solution.
3. **`01_Spring_Boot_Basics`:** Slides, Demo.
4. **`02_Web_REST`:** Slides, Demo, Assignment, Solution.
5. **`03_Data_JPA`:** Slides, Demo, Assignment, Solution.
6. **`04_Testing` und `05_Betrieb`:** Slides, Demos, Assignment und Solution
   für Testing.

Schritt 1 zuerst, damit jedes neue Modul in die endgültige Struktur fällt.
Die Schritte 2–6 sind untereinander unabhängig; es gibt keine geteilten
Klassen zwischen den Demos.

Die Umbenennung des GitLab-Repos liegt außerhalb des Arbeitsverzeichnisses
und erfolgt per `glab` — aber erst nach Rückfrage, da sie fremde Clones
betrifft.
