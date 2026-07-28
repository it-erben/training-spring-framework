# Übung 00: Spring Core — Beans und Dependency Injection

Ihr baut die Verdrahtung einer kleinen Kursverwaltung: Alle Klassen sind fertig geschrieben, aber der Spring-Container kennt noch keine einzige davon. Eure Aufgabe ist es, die Klassen als Beans zu deklarieren und die Abhängigkeiten per Konstruktor-Injection zu verdrahten — bis alle vier mitgelieferten Tests grün sind.

## Lernziele

- Klassen mit Stereotyp-Annotationen (`@Service`, `@Repository`, `@Component`) als Beans deklarieren
- Abhängigkeiten per Konstruktor-Injection beziehen
- Mehrdeutigkeit zwischen zwei Beans desselben Typs mit `@Primary` und `@Qualifier` auflösen
- Fremdklassen ohne eigene Annotation über `@Configuration` und `@Bean` registrieren

## Voraussetzungen

- Java 21, Maven 3.8+
- Inhalte aus Modul *00 Spring Core* (Folien und Demo `demos/sb-basics-core-demo`)

## Was liegt bereit?

Alle Klassen liegen in `src/main/java` unter `tech.erben.springboot.basics.core.task`:

| Klasse | Zustand |
| --- | --- |
| `CourseAdminApplication` | Fertig — Einstiegspunkt mit `@SpringBootApplication` |
| `Course`, `Trainer` | Fertig — Records für die Fachdaten |
| `CourseCatalog` | Fertig — Interface für den Katalogzugriff |
| `InMemoryCourseCatalog` | Logik fertig, **Bean-Deklaration fehlt** |
| `FeeCalculator` | Fertig — Interface für die Gebührenberechnung |
| `GrossFeeCalculator` | Logik fertig, **Bean-Deklaration und Standard-Markierung fehlen** |
| `NetFeeCalculator` | Logik fertig, **Bean-Deklaration fehlt** |
| `CourseService` | Methodenrümpfe fertig, **Bean-Deklaration und Konstruktor fehlen** |
| `TrainerDirectory` | „Fremdklasse" — nicht verändern! |
| `TrainerConfig` | Methode fertig, **`@Configuration` und `@Bean` fehlen** |
| `CourseServiceTest` | Die vier Tests, die den Fortschritt messen |

Der Startzustand ist **absichtlich rot**: `mvn test -DskipAssignmentTests=false` schlägt fehl, weil der Container keine der benötigten Beans findet. Jede gelöste Aufgabe bringt euch näher an Grün. Die `TODO`-Kommentare im Code markieren alle Stellen.

## Aufgaben

1. **Katalog und Service als Beans verdrahten** (Test: *Aufgabe 1*)
   - Deklariert `InMemoryCourseCatalog` mit einer passenden Stereotyp-Annotation als Bean.
   - Deklariert `CourseService` als Bean und schreibt den Konstruktor, der alle drei Abhängigkeiten entgegennimmt und den Feldern zuweist.
   - Hinweis: Bei genau einem Konstruktor injiziert Spring automatisch — ein `@Autowired` ist nicht nötig.
2. **Brutto-Rechner zum Standard machen** (Test: *Aufgabe 2*)
   - Deklariert `GrossFeeCalculator` als Bean.
   - Sorgt dafür, dass diese Bean gewinnt, wenn ein `FeeCalculator` ohne weitere Angabe injiziert wird.
3. **Netto-Rechner gezielt auswählen** (Test: *Aufgabe 3*)
   - Deklariert `NetFeeCalculator` als Bean mit dem Namen `netFeeCalculator`.
   - Wählt diese Bean im Konstruktor des `CourseService` per `@Qualifier` für das Feld `netCalculator` aus.
4. **Fremdklasse per `@Bean`-Methode bereitstellen** (Test: *Aufgabe 4*)
   - `TrainerDirectory` steht stellvertretend für eine Klasse aus einer fremden Bibliothek — ihr dürft sie nicht verändern und könnt sie daher nicht annotieren.
   - Vervollständigt stattdessen `TrainerConfig`, damit der Container die vorbereitete Methode als Bean-Fabrik nutzt.

Zwischenstand jederzeit prüfen:

```bash
mvn test -DskipAssignmentTests=false
```

## Bonusaufgabe (optional)

- Deklariert `RegistrationCounter` als Bean mit `@Scope("prototype")`.
- Weist nach, dass der Container pro Anfrage eine neue Instanz erzeugt: Holt euch im Test (oder über einen `CommandLineRunner`) zweimal eine Instanz — z. B. per `ObjectProvider<RegistrationCounter>` — und vergleicht die `instanceNumber()`. Was passiert, wenn ihr `@Scope("prototype")` wieder entfernt?

## Erfolgskriterien

- [ ] `mvn test -DskipAssignmentTests=false` läuft grün — alle vier Tests in `CourseServiceTest` bestehen
- [ ] `TrainerDirectory` ist unverändert geblieben
- [ ] Der `CourseService` bezieht alle Abhängigkeiten über den Konstruktor — keine Feld-Injection

## Lösung

Siehe `solutions/sb-basics-core-solution`.
