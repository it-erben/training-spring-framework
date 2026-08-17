# Übung 04: Testing

Der Produktivcode der Kursverwaltung ist in dieser Aufgabe bereits komplett
fertig. Was fehlt, sind die Tests: `src/test/java` ist leer. Ihr füllt es mit
allen drei Testarten aus der Demo. Der Maßstab ist dabei nicht, dass eure Tests
grün sind, sondern dass sie rot werden **können**: Am Ende verändert ihr den
Produktivcode absichtlich und prüft, ob eure Tests den Fehler fangen.

## Lernziele

- Einen Unit-Test ohne Spring-Kontext mit `@Mock`, `@InjectMocks`, Stubbing und
  `verify` schreiben
- Mit `@WebMvcTest` und `@MockitoBean` nur die MVC-Schicht testen: Statuscodes
  und Header, ohne Service und Datenbank
- Mit `@SpringBootTest`, `RANDOM_PORT` und `TestRestTemplate` einen Ablauf
  Ende-zu-Ende durch die ganze Anwendung prüfen
- Die Qualität einer Testsuite mit einer Mutationsprobe hinterfragen: Fangen
  meine Tests echte Fehler?

## Voraussetzungen

- Java 21, Maven 3.8+
- Inhalte aus Modul *04 Testing* (Folien und Demo
  `demos/sb-basics-testing-demo`)

## Was liegt bereit?

Alle Klassen liegen fertig in `src/main/java` unter
`tech.erben.springboot.basics.testing.task`. Hier ändert ihr nichts (bis auf die
Mutationsprobe am Ende):

| Klasse                                            | Rolle                                                                                                                                |
|---------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| `CourseAdminApplication`                          | Einstiegspunkt mit `@SpringBootApplication`                                                                                          |
| `CourseSeeder`                                    | Legt beim Start zwei Kurse an: `SPRING-BASICS` (12 Plätze) und `SPRING-COMPACT` (1 Platz) — eure Testdaten für Aufgabe 3             |
| `Course`, `Participant`                           | Die Entities aus Modul 03, `seats` legt die Kapazität fest                                                                           |
| `CourseRepository`, `ParticipantRepository`       | Derived Queries: `findByCode` und `countByCourse`                                                                                    |
| `ParticipantService`                              | Die Fachlogik: `register(courseCode, email)` meldet an, solange freie Plätze da sind und gibt `false` zurück, wenn der Kurs voll ist |
| `ParticipantController`                           | `POST /api/courses/{code}/participants` — `201` mit Location-Header bei Erfolg, `409` bei ausgebuchtem Kurs                          |
| `RegistrationRequest`                             | Der Request-Body: `{"email": "..."}`                                                                                                 |
| `CourseNotFoundException`, `RestExceptionHandler` | Unbekannter Kurscode → `404`                                                                                                         |

Auch die POM ist fertig: Alle Test-Abhängigkeiten (`spring-boot-starter-test`,
`spring-boot-starter-webmvc-test`, `spring-boot-starter-restclient` und
`-restclient-test`) liegen schon bereit. Ihr konzentriert euch aufs Testen.

## Aufgaben

1. **Unit-Test für `ParticipantService.register`** (Vorbild: `BookServiceTest`
   der Demo)
    - Legt eine Testklasse mit `@ExtendWith(MockitoExtension.class)` an, mockt
      beide Repositories mit `@Mock` und lasst euch den Service mit
      `@InjectMocks` bauen.
    - Testfall 1: Es sind freie Plätze da (stubbt `findByCode` und
      `countByCourse` passend), `register` gibt `true` zurück und speichert
      einen Teilnehmer.
    - Testfall 2: Der Kurs ist voll: `register` gibt `false` zurück und mit
      `verify(..., never())` weist ihr nach, dass **nichts** gespeichert wird.
    - Testet die Bedingung an ihrer Grenze: Der interessante Fall ist nicht der
      halb leere Kurs, sondern der letzte freie Platz gegen den vollen Kurs.
2. **`@WebMvcTest` für `ParticipantController`** (Vorbild:
   `BookControllerWebMvcTest`)
    - Startet mit `@WebMvcTest(ParticipantController.class)` nur die MVC-Schicht
      und ersetzt den `ParticipantService` per `@MockitoBean`.
    - Testfall 1: Das Mock meldet Erfolg (`true`) - der `POST` liefert `201` und
      den Location-Header.
    - Testfall 2: Das Mock meldet ausgebucht (`false`) - der `POST` liefert
      `409`.
3. **Integrationstest mit `TestRestTemplate`** (Vorbild:
   `BookstoreIntegrationTest`)
    - Startet die ganze Anwendung mit
      `@SpringBootTest(webEnvironment = RANDOM_PORT)` und
      `@AutoConfigureTestRestTemplate`.
    - Meldet per echtem HTTP-Request einen Teilnehmer an und prüft die Antwort —
      die beim Start angelegten Kurse (`SPRING-BASICS`, `SPRING-COMPACT`) sind
      eure Testdaten.
    - Tipp: Der Kurs `SPRING-COMPACT` hat genau einen Platz. Meldet zweimal an —
      erst wenn die zweite Anmeldung mit `409` abgelehnt wird, ist bewiesen,
      dass die erste wirklich in der Datenbank gelandet ist.

## Bonusaufgabe (optional)

- Testet den dritten Ausgang von `register`: Bei unbekanntem Kurscode fliegt
  eine `CourseNotFoundException`, die der `RestExceptionHandler` in `404`
  übersetzt. Im Unit-Test fangt ihr die Exception mit `assertThatThrownBy` aus
  AssertJ, im `@WebMvcTest` lasst ihr das Mock sie mit `thenThrow` werfen und
  erwartet den Status `404`.

## Lösung

Siehe `solutions/sb-basics-testing-solution`.
