# Arbeitsregeln

Ton, Schreibweise und Commit-Regeln stehen in der Nutzer-Konfiguration
(`~/.claude/CLAUDE.md`, Abschnitte "Schreibweise in deutschen Texten" und
"Arbeitsregeln in Repos"). Hier steht nur, was in diesem Repo dazukommt oder
abweicht.

## Folien

Folien sind Lehrmaterial. Die Regel gegen Personalpronomen und Leseransprache
gilt hier nicht. Referenz sind `slides/00_` bis `05_`, `09_Wiederholung`,
`18_Batch` und `19_Batch_Betrieb`. Die Advanced-Module `10_` bis `17_` sind
älter und weichen ab; als Vorlage taugen sie nicht.

- Erwünscht: "wir" für das gemeinsame Vorgehen, "ihr/euch" für die
  Teilnehmer, Fragen als Überschrift, nummerierte Schrittlisten.
- Motivation vor Mechanik. Erst das Problem ohne Framework, dann die Lösung.
  Eine Folie, die mit der Annotation beginnt, beantwortet die falsche Frage.
- Prosa-Anlauf von ein bis zwei Sätzen, dann Code, dann Bullets, die den Code
  deuten. Bullets, die den Code nacherzählen, streichen.
- Bullets beginnen mit dem fetten Stichwort: `**Speicher:** ...`.
- Fehlerfälle mit dem Namen, den die Teilnehmer im Log sehen
  (`NoUniqueBeanDefinitionException`), nicht mit "schlägt fehl".
- Zahlen statt Adjektiven. Gemessene Werte, Prozentsätze, Grenzen.
- Tabellen beantworten "wann was", nicht "was kann es".
- Modulaufbau: `# Titel` → `## In diesem Modul` → `#`-Trennfolien ohne Inhalt →
  Fachfolien → `# Demo` / `# Übung` → Pfad auf `demos/` bzw. `assignments/`.
- Querverweise auf andere Module als `Modul *11_Configuration*`.
- Zu volle Folien über die Dichte-Klassen `dense`, `denser` und `densest`
  verkleinern, nicht durch Textkürzung. Prüfen mit
  `tools/check-slide-overflow.mjs`.

## Aufbau dieses Repos

Zwei Blöcke, getrennt über die Nummerierung der Slide-Verzeichnisse:

- `slides/00_` bis `05_`: Basis-Block, 2 Tage, Marp-Header
  `Spring Boot Basics`, Maven-Module mit Präfix `sb-basics-`.
- `slides/09_` bis `19_`: Advanced-Block, 3 Tage, Marp-Header
  `Spring Boot Advanced`, Maven-Module mit Präfix `sb-advanced-`.
  `09_Wiederholung` ist die Brücke zwischen beiden Blöcken: eine Übung, die
  den Basis-Block ohne neuen Stoff wiederholt.

Der Basis-Block setzt keine Spring-Kenntnisse voraus. Mehrere
Advanced-Module beginnen mit "Wiederholung:"-Slides, die sich auf ihn
beziehen. Eine Änderung im Basis-Block kann dort eine Lücke reißen.

`assignments` enthält Übungsaufgaben, `solutions` deren Lösungen. Beide
bleiben synchron.

## Fallstricke dieses Repos

- **Die `TODO`-Marken in `*-start`-Modulen und Assignments sind Lehrmaterial
  und bleiben stehen.** Sie markieren die Handgriffe des Live-Codings. Die
  Regel zum Entfernen von TODO-Markern gilt für sie nicht.
- **Assignments, die Tests mitbringen, starten im Ausgangszustand rot** und
  werden im Gesamtbuild über die Property `skipAssignmentTests`
  übersprungen. Kein Assignment-Test darf im Ausgangszustand grün sein.
  Sichtbar machen mit `-DskipAssignmentTests=false`. Nicht jedes Assignment
  bringt Tests mit: `sb-advanced-actuator-micrometer-assignment` hat kein
  Testverzeichnis, die Prüfung läuft laut seiner README über `curl`;
  `sb-advanced-testing-assignment` hat kein `pom.xml` und keinen Eintrag in
  `assignments/pom.xml`, der `skipAssignmentTests`-Mechanismus greift dort
  strukturell nicht. In `sb-advanced-data-mongodb-assignment` und
  `sb-advanced-data-jpa-assignment` hängt an derselben Property auch das
  Kompilieren der Tests (`default-testCompile`), nicht nur ihre Ausführung.
  Die Tests referenzieren Klassen, die erst die Übung anlegt. Dort schlägt
  `-DskipAssignmentTests=false` am Compile fehl, nicht erst am Test.
- **Jeder Zwischenschritt** einer `-start`-README muss lauffähig sein. Ein
  `package`-Lauf beweist das nicht. Die Schritte einzeln anwenden und
  starten.
- **Im Basis-Block und im Batch-Modul nutzen Demos eine Buchhandlung, Übungen
  eine Kursverwaltung.** Die Trennung verhindert, dass die Demo-Lösung in die
  Übung kopierbar ist. Der übrige Advanced-Block folgt keiner einheitlichen
  Domäne.
- Kein Lombok in den Basis-Modulen. Kein `spring-boot-starter-parent`: Das
  Root-POM liefert nur gemeinsame Properties (`spring-boot.version`,
  `spring-cloud.version`, `maven-compiler-plugin.version`,
  `project.build.sourceEncoding`, Compiler-Version und -Ziel,
  `datafaker.version`), jedes Leaf-POM importiert `spring-boot-dependencies`
  selbst. Ein
  lauffähiges Fat-JAR braucht deshalb eine explizite `repackage`-Execution.
- **Spring Batch 6 hat die Pakete umgebaut.** `Job`, `JobExecution`, `Step`
  und `StepExecution` liegen in `org.springframework.batch.core.job` und
  `...core.step`, `JobParameters` in `...core.job.parameters`, sämtliche Reader
  und Writer in `org.springframework.batch.infrastructure.item.*`.
  `JobBuilderFactory` und `StepBuilderFactory` gibt es nicht mehr. Jedes
  Beispiel aus Büchern und dem Netz zeigt die alten Pakete. Code für die
  `sb-advanced-batch-`-Module und `slides/18_Batch` gegen die Jars prüfen,
  nicht aus dem Gedächtnis schreiben.
- **Die CI läuft auf zwei Plattformen.** `.gitlab-ci.yml` bindet die
  GitLab-Komponenten ein, `.github/workflows/ci.yml` ruft `lint.yml`,
  `slides.yml`, `maven.yml`, `release.yml` und `pages.yml` aus
  `it-erben/ci`. Die PDFs gehen
  dort auf GitHub Pages, ein Deployment gibt es auf GitHub nicht.
