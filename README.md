# Spring Boot Schulung

Fünftägige Schulung in zwei Blöcken, die auch einzeln buchbar sind.

| Block | Dauer | Voraussetzung |
| --- | --- | --- |
| **Basis** (Module 00–05) | 2 Tage | Java 21, keine Spring-Erfahrung |
| **Advanced** (Module 10–17) | 3 Tage | Basis-Block oder gleichwertige Praxis |

Der Basis-Block holt Java-Entwickler ab, die noch nie mit Spring gearbeitet
haben, und führt sie bis zu einer containerisierten REST-Anwendung mit
Datenbank und Tests. Der Advanced-Block setzt genau dort an.

## Basis-Block

| Modul | Dauer | Slides | Demo | Übung |
| --- | --- | --- | --- | --- |
| Spring Core | 2,5 h | `slides/00_Spring_Core` | `demos/sb-basics-core-demo` | `assignments/sb-basics-core-assignment` |
| Spring Boot Basics | 2 h | `slides/01_Spring_Boot_Basics` | `demos/sb-basics-boot-demo` | — |
| Web und REST | 2,5 h | `slides/02_Web_REST` | `demos/sb-basics-web-demo` | `assignments/sb-basics-web-assignment` |
| Data und JPA | 2,5 h | `slides/03_Data_JPA` | `demos/sb-basics-data-jpa-demo` | `assignments/sb-basics-data-jpa-assignment` |
| Testing | 1,5 h | `slides/04_Testing` | `demos/sb-basics-testing-demo` | `assignments/sb-basics-testing-assignment` |
| Betrieb | 1 h | `slides/05_Betrieb` | `demos/sb-basics-operations-demo` | — |

Die Lösungen liegen unter `solutions/sb-basics-*-solution`.

## Advanced-Block

| Modul | Slides |
| --- | --- |
| Microservice-Architektur | `slides/10_Microservices_Architecture` |
| Configuration und Internals | `slides/11_Configuration` |
| Testing | `slides/12_Testing` |
| Data und Persistence | `slides/13_Data` |
| Web und REST | `slides/14_Web` |
| Actuator und Observability | `slides/15_Actuator` |
| Security | `slides/16_Security` |
| Messaging | `slides/17_Messaging` |

Demos, Übungen und Lösungen des Advanced-Blocks tragen das Präfix
`sb-advanced-`.

## Fachlichkeiten

Damit Teilnehmer sich nicht in jedem Modul neu eindenken müssen, ziehen sich
zwei Domänen durch den Basis-Block:

- **Demos** verwenden eine Buchhandlung (`Book`, `Author`, `Order`).
- **Übungen** verwenden eine Kursverwaltung (`Course`, `Participant`,
  `Trainer`).

Die Trennung ist Absicht: Sie verhindert, dass sich die Demo-Lösung in die
Übung kopieren lässt.

## Voraussetzungen

- Java 21
- Maven 3.9
- Docker (optional, für das Container-Beispiel in Modul 05 und die
  Testcontainers-Tests im Advanced-Block)

## Bauen und Prüfen

```bash
mvn verify
```

Baut alle Module und führt alle Tests aus. Der erste Lauf zieht
Docker-Images für die Testcontainers-Tests des Advanced-Blocks und dauert
entsprechend.

Nur die Basis-Module:

```bash
mvn verify -pl "$(ls -d demos/sb-basics-* assignments/sb-basics-* \
    solutions/sb-basics-* | paste -sd, -)" -am
```

### Übungen

Die Übungen bringen Tests mit, die im Ausgangszustand **absichtlich rot**
sind — sie sind Aufgabenstellung, nicht Regression. Im Gesamtbuild werden
sie deshalb übersprungen. Wer den roten Ausgangszustand sehen will:

```bash
mvn test -pl assignments/sb-basics-core-assignment -DskipAssignmentTests=false
```

Ausnahme ist `sb-basics-testing-assignment`: Dort ist der Produktivcode
vollständig und das Testverzeichnis leer — die Teilnehmer schreiben die
Tests selbst. Das Erfolgskriterium ist dort eine Mutationsprobe, die in der
README der Übung beschrieben ist.

### Live-Coding-Demos

Vier Demos liegen als Paar aus `-start` und `-finished` vor. Das
`-start`-Modul ist der Ausgangszustand für die Vorführung, jeder Handgriff
ist im Code mit einer `TODO`-Marke versehen, und die README des Demo-Moduls
beschreibt die Schrittfolge. Jeder Zwischenschritt ist lauffähig.

## Slides bauen

Die Decks sind [Marp](https://marp.app/)-Markdown. Die CI erzeugt daraus
PDFs; lokal genügt die Marp-Erweiterung für VS Code oder:

```bash
npx @marp-team/marp-cli slides/00_Spring_Core/slides.md --pdf
```
