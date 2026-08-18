# Übung 00: Spring Core — Beans und Dependency Injection

Ihr implementiert eine kleine Kursverwaltung fertig: Alle Klassen sind
geschrieben, aber der Spring-Container hat noch keine Kenntnis von ihnen. Eure
Aufgabe ist es, die Klassen als Beans zu deklarieren und die Abhängigkeiten per
Konstruktor-Injection einzufügen, bis der mitgelieferte Test grün ist.

## Lernziele

- Klassen mit Stereotyp-Annotationen (`@Service`, `@Repository`, `@Component`)
  als Beans deklarieren
- Abhängigkeiten per Konstruktor-Injection einfügen
- Mehrdeutigkeit zwischen zwei Beans desselben Typs mit `@Primary` und
  `@Qualifier` auflösen
- Fremdklassen ohne eigene Annotation über `@Configuration` und `@Bean`
  registrieren

## Voraussetzungen

- Java 21, Maven 3.8+
- Inhalte aus Modul *00 Spring Core* (Folien und Demo
  `demos/sb-basics-core-demo`)

## Was liegt bereit?

Alle Klassen liegen in `src/main/java` unter
`tech.erben.springboot.basics.core.task`:

| Klasse                   | Zustand                                                            |
|--------------------------|--------------------------------------------------------------------|
| `CourseAdminApplication` | Fertig - Einstiegspunkt mit `@SpringBootApplication`               |
| `Course`, `Trainer`      | Fertig - Records für die Fachdaten                                 |
| `CourseCatalog`          | Fertig - Interface für den Katalogzugriff                          |
| `InMemoryCourseCatalog`  | Logik fertig, **Bean-Deklaration fehlt** (`@Repository`)           |
| `FeeCalculator`          | Fertig - Interface für die Gebührenberechnung                      |
| `GrossFeeCalculator`     | Logik fertig, **Bean-Deklaration und Standard-Markierung fehlen**  |
| `NetFeeCalculator`       | Logik fertig, **Bean-Deklaration fehlt**                           |
| `CourseService`          | Methodenrümpfe fertig, **Bean-Deklaration und Konstruktor fehlen** |
| `TrainerDirectory`       | "Fremdklasse" - nicht verändern!                                   |
| `TrainerConfig`          | Methode fertig, **`@Configuration` und `@Bean` fehlen**            |
| `CourseServiceTest`      | Der Test, der den Fortschritt misst                                |

Der Startzustand ist **absichtlich defekt**:
`mvn test -DskipAssignmentTests=false`
schlägt fehl, weil der Container keine der benötigten Beans findet.

Es gibt genau einen Test, und der wird erst grün, wenn alle vier Aufgaben gelöst
sind: Fehlt eine Bean, startet der Anwendungskontext gar nicht erst. Euer
Fortschritt zeigt sich bis dahin in der Fehlermeldung - die
`NoSuchBeanDefinitionException` nennt immer die **nächste** fehlende Bean.
Sobald der Kontext startet, listet der Fehlerbericht alle noch offenen Aufgaben
auf einmal. Die `TODO`-Kommentare im Code markieren alle Stellen.

## Aufgaben

1. **Katalog und Service als Beans aufsetzen** (Zusicherung *Aufgabe 1*)
    - Bitte deklariert `InMemoryCourseCatalog` mit `@Repository` als Bean.
    - Bitte deklariert außerdem `CourseService` als Bean und schreibt den
      Konstruktor, der alle drei Abhängigkeiten entgegennimmt und den Feldern
      zuweist.
    - Hinweis: Bei genau einem Konstruktor injiziert Spring automatisch. Ein
      `@Autowired` ist nicht nötig.
2. **Brutto-Rechner zum Standard machen** (Zusicherung *Aufgabe 2*)
    - Deklariert `GrossFeeCalculator` als Bean.
    - Sorgt dafür, dass diese Bean gewinnt, wenn ein `FeeCalculator` ohne
      weitere Angabe injected wird.
3. **Netto-Rechner gezielt auswählen** (Zusicherung *Aufgabe 3*)
    - Deklariert `NetFeeCalculator` als Bean mit dem Namen `netFeeCalculator`.
    - Wählt diese Bean im Konstruktor des `CourseService` per
      `@Qualifier("netFeeCalculator")` für das Feld `netCalculator` aus.
4. **Fremdklasse per `@Bean`-Methode bereitstellen** (Zusicherung *Aufgabe 4*)
    - `TrainerDirectory` steht stellvertretend für eine Klasse aus einer fremden
      Bibliothek - ihr dürft sie nicht verändern und könnt sie daher nicht
      annotieren.
    - Vervollständigt stattdessen `TrainerConfig`, damit der Container die
      vorbereitete Methode als Bean-Fabrik nutzt.

Zwischenstand jederzeit prüfen:

```bash
mvn test -DskipAssignmentTests=false
```

## Bonusaufgabe (optional)

- Deklariert `RegistrationCounter` als Bean (`@Component`) mit
  `@Scope("prototype")`.
- Weist nach, dass der Container pro Anfrage eine neue Instanz erzeugt: Holt
  euch im Test (oder über einen `CommandLineRunner`) zweimal eine Instanz, z.B.
  per `ObjectProvider<RegistrationCounter>` und vergleicht die
  `instanceNumber()`. Was passiert, wenn ihr `@Scope("prototype")` wieder
  entfernt?

## Lösung

Siehe `solutions/sb-basics-core-solution`.
