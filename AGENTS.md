# AGENTS

## Linting

Diese Repos verwenden Pre-Commit Hooks:

- Markdown: `markdownlint-cli2` mit `--fix`, wobei die line-length maximal 80 ist
- YAML: `yamllint` (extends relaxed, line-length max 140)
- Links: `lychee` mit `--accept 429,200`, `--exclude http://localhost.*`, `--exclude-path .npm-cache`,
  `--max-concurrency 4`, `--retry-wait-time 2`, `--timeout 20`, `--cache`

Bitte halte `.pre-commit-config.yaml` und diese Einstellungen synchron, wenn du die Linter- oder Link-Check-Regeln
aenderst.

## Struktur

- Im Verzeichnis `slides` befinden sich Verzeichnisse für Module einer Spring-Boot-Schulung, jeweils mit einer datei
  `slides.md` welche Marp-Slides enthält.

- `assignments` enthält Übungsaufgaben
- `solutions` deren Lösungen.

Halte die Lösungen in solutions immer synchron mit den assignments.

### Zwei Blöcke

Die Schulung besteht aus einem Basis- und einem Advanced-Block. Die
Nummerierung der Slide-Verzeichnisse trennt beide:

- `00_` bis `05_` — Basis-Block, 2 Tage, Marp-Header `Spring Boot Basics`.
  Maven-Module tragen das Präfix `sb-basics-`.
- `10_` bis `17_` — Advanced-Block, 3 Tage, Marp-Header
  `Spring Boot Advanced`. Maven-Module tragen das Präfix `sb-advanced-`.

Der Basis-Block setzt keine Spring-Kenntnisse voraus. Was dort eingeführt
wird, darf der Advanced-Block als bekannt voraussetzen — mehrere
Advanced-Module beginnen mit „Wiederholung:"-Slides, die sich darauf
beziehen. Wer im Basis-Block etwas ändert, sollte prüfen, ob ein
Advanced-Modul darauf aufbaut.

### Fachlichkeiten

Im Basis-Block verwenden **Demos** eine Buchhandlung (`Book`, `Author`,
`Order`), **Übungen** eine Kursverwaltung (`Course`, `Participant`,
`Trainer`). Diese Trennung ist Absicht und darf nicht aufgeweicht werden:
Sie verhindert, dass die Demo-Lösung in die Übung kopierbar ist.

### Konventionen der Basis-Module

- Kein Lombok. Records für DTOs, explizite Getter/Setter für Entities.
- Kein `spring-boot-starter-parent`; die Boot-Version kommt per BOM-Import
  aus dem Root-POM. Ein lauffähiges Fat-JAR braucht deshalb eine explizite
  `repackage`-Execution.
- Assignment-Tests sind im Ausgangszustand rot und werden im Gesamtbuild
  über die Property `skipAssignmentTests` übersprungen. Kein Assignment-Test
  darf im Ausgangszustand grün sein — ein Test, der besteht, bevor die
  Aufgabe gelöst ist, taugt nicht als Lernkontrolle.
- Demo-Module mit `-start`/`-finished`-Paar: Jeder Zwischenschritt der
  README muss lauffähig sein, und jeder Handgriff braucht eine `TODO`-Marke
  im Code.
