# Arbeitsregeln

## Ton

- Knapp. Sag, was zu sagen ist, dann Schluss. Kein Vorgeplänkel, keine
  Zusammenfassung des gerade Getanen, kein „gute Frage“, kein Wiederholen der
  Aufgabe.
- Keine Füll-Adjektive (robust, nahtlos, mächtig, umfassend, produktionsreif).
  Knapp sagen, was der Code tut, nicht wie gut er ist. Nicht paraphrasieren, was
  die nächsten Zeilen tun. Stattdessen das WARUM und WIE erklären, wenn das dem
  Verständnis wirklich hilft.
- Docs und READMEs: was es ist, wie man es nutzt, was es bereitstellt. Sonst
  nichts.
- Commit-Nachrichten: conventional-commit, Imperativ, möglichst einzeilig. Den
  Scope richtig wählen — Release-Tooling routet unter Umständen darüber. Breaking
  Changes bekommen ein `!` (`feat(api)!: …`) oder einen `BREAKING CHANGE:`-Footer.
  Betreffzeile ≤ 72 Zeichen, Imperativ („add“, „fix“, nicht „added“, „fixes“).
  Body auf 72 Zeichen umbrechen.
- Kleine, fokussierte Commits bevorzugen. Release-Tooling leitet Versionssprünge
  und Changelog oft aus den Commit-Betreffzeilen ab.
- Keine Ticket-Nummern in Code, Commits oder Docs.
- Kommentare erklären das *Warum*, nicht das *Was*. Code-Kommentare benennen die
  Absicht oder eine Einschränkung, die der Code nicht zeigen kann. Kommentare
  löschen, die den Code nur wiederholen.
- Kommentare und Docs immer als Ganzes betrachten. Nie nur anhängen. Im Kontext
  prüfen und auf den faktischen Stand bringen. Im Zweifel im Code recherchieren.
  Veraltete und aus dem Kontext gefallene Verweise entfernen, ebenso frühere
  Beobachtungen, Schilderungen von Situationen, die zu einer früheren Änderung
  führten, Maschinennamen oder -adressen sowie jede Vermutung über die
  nachgelagerte Nutzung dieses Repos und seiner Artefakte — abgesehen von
  gültigen, aktuellen Beispielen.
- Auf ein anderes Repository oder Projekt nur verweisen, wenn dessen Zustand der
  unmittelbare Grund für die Änderung ist (ein Dependency-Bump, ein eingespielter
  Fix, ein an eine veröffentlichte Version gebundener API-Vertrag). Kontext für
  Reviewer, Dank oder Querverweise gehören in den PR-Thread oder ein Issue, nicht
  in den Commit.
- Deklarative Fakten schreiben. Keine Personalpronomen („ich“, „wir“, „du“).
  Keine Leseransprache: kein „beachte, dass…“, „wie man sieht…“, „wir haben uns
  entschieden…“, „das sollte helfen…“. Die Regel gilt für Dokumentation, die
  ein Artefakt beschreibt. Ausgenommen sind Texte, die Teilnehmer zum Handeln
  auffordern — Aufgabenstellungen und die Schritt-für-Schritt-Anleitungen der
  `*-start`-Module —, unabhängig vom Verzeichnis, in dem sie liegen.
  Maßgeblich ist die Textsorte: Eine README, die als Aufgabenstellung
  formuliert ist, fällt auch unter die Ausnahme, wenn sie unter `demos/` oder
  `solutions/` liegt statt unter `assignments/`.
- Nicht erzählen. Keine Historie, was zuerst versucht wurde, was scheiterte oder
  welche Alternativen erwogen wurden.
- Keine Füll-Verben ohne Konkretes. „Aufräumen“, „verbessern“, „refactoren“
  allein sagen nichts; entweder die tatsächliche Änderung benennen oder die Zeile
  weglassen.
- Keine Checklisten, keine „Summary“-/„Test plan“-Abschnitte, keine
  Marketing-Sprache, keine Emojis.

## Folien

Der Abschnitt „Ton“ gilt für Dokumentation, die ein Artefakt beschreibt.
Folien sind Lehrmaterial und folgen eigenen Regeln. Referenz sind
`slides/00_` bis `05_`, `09_Wiederholung`, `18_Batch` und `19_Batch_Betrieb`.
Die Advanced-Module `10_` bis `17_` sind älter und weichen ab; als Vorlage
taugen sie nicht.

- Erwünscht: „wir“ für das gemeinsame Vorgehen, „ihr/euch“ für die
  Teilnehmer, Fragen als Überschrift, nummerierte Schrittlisten.
- Motivation vor Mechanik. Erst das Problem ohne Framework, dann die Lösung.
  Eine Folie, die mit der Annotation beginnt, beantwortet die falsche Frage.
- Prosa-Anlauf von ein bis zwei Sätzen, dann Code, dann Bullets, die den Code
  deuten. Bullets, die den Code nacherzählen, streichen.
- Bullets beginnen mit dem fetten Stichwort: `**Speicher:** …`.
- Fehlerfälle mit dem Namen, den die Teilnehmer im Log sehen
  (`NoUniqueBeanDefinitionException`), nicht mit „schlägt fehl“.
- Zahlen statt Adjektiven. Gemessene Werte, Prozentsätze, Grenzen.
- Tabellen beantworten „wann was“, nicht „was kann es“.
- Modulaufbau: `# Titel` → `## In diesem Modul` → `#`-Trennfolien ohne Inhalt →
  Fachfolien → `# Demo` / `# Übung` → Pfad auf `demos/` bzw. `assignments/`.
- Querverweise auf andere Module als `Modul *11_Configuration*`.
- Kein Ausrufezeichen, keine Emojis, keine Füll-Adjektive. Hier gilt „Ton“
  unverändert.
- Zu volle Folien über die Dichte-Klassen `dense`, `denser` und `densest`
  verkleinern, nicht durch Textkürzung. Prüfen mit
  `tools/check-slide-overflow.mjs`.

## Vor dem Abschluss

- Lint, Tests und Build des Projekts für alles Berührte ausführen.
- Nicht „fertig“ behaupten, ohne die Prüfung ausgeführt zu haben. Belege vor
  Behauptungen.
- Alle TODO-Marker entfernen, die du in deiner Sitzung hinzugefügt hast, und
  nacharbeiten — oder dem Nutzer sagen, dass ein Follow-up nötig ist. Alle Marker
  und Verweise auf deine eigene Aufgabenliste oder historische Arbeitsschritte
  (P2, P3a, Item 1, Task A usw.) samt ihrer Erzählung entfernen. Wenn wirklich
  etwas offen bleibt, dem Nutzer außerhalb von Code, Docs, Markdown, Kommentaren,
  PR-Beschreibungen, Commit-Nachrichten oder allem anderen in diesem Repo und
  seiner angeschlossenen Pipeline Bescheid geben.

## Aufbau dieses Repos

Zwei Blöcke, getrennt über die Nummerierung der Slide-Verzeichnisse:

- `slides/00_` bis `05_` — Basis-Block, 2 Tage, Marp-Header
  `Spring Boot Basics`, Maven-Module mit Präfix `sb-basics-`.
- `slides/09_` bis `19_` — Advanced-Block, 3 Tage, Marp-Header
  `Spring Boot Advanced`, Maven-Module mit Präfix `sb-advanced-`.
  `09_Wiederholung` ist die Brücke zwischen beiden Blöcken: eine Übung, die
  den Basis-Block ohne neuen Stoff wiederholt.

Der Basis-Block setzt keine Spring-Kenntnisse voraus. Mehrere
Advanced-Module beginnen mit „Wiederholung:"-Slides, die sich auf ihn
beziehen — eine Änderung im Basis-Block kann dort eine Lücke reißen.

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
  Kompilieren der Tests (`default-testCompile`), nicht nur ihre Ausführung —
  die Tests referenzieren Klassen, die erst die Übung anlegt. Dort schlägt
  `-DskipAssignmentTests=false` am Compile fehl, nicht erst am Test.
- **Jeder Zwischenschritt** einer `-start`-README muss lauffähig sein. Ein
  `package`-Lauf beweist das nicht — die Schritte einzeln anwenden und
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
  `…core.step`, `JobParameters` in `…core.job.parameters`, sämtliche Reader
  und Writer in `org.springframework.batch.infrastructure.item.*`.
  `JobBuilderFactory` und `StepBuilderFactory` gibt es nicht mehr. Jedes
  Beispiel aus Büchern und dem Netz zeigt die alten Pakete. Code für die
  `sb-advanced-batch-`-Module und `slides/18_Batch` gegen die Jars prüfen,
  nicht aus dem Gedächtnis schreiben.
