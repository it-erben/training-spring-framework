# Demo: Testing — Unit-, Slice- und Integrationstests

Diese Demo testet eine kombinierte Buchhandlung aus den bisherigen
Modulen: ein `BookController` (Modul 02), ein `BookService` mit einer
Rabattregel und ein `BookRepository` gegen In-Memory-H2 (Modul 03). An
dieser Anwendung entstehen die drei Testarten des Spring-Boot-Baukastens:
ein Unit-Test mit Mockito ganz ohne Spring, ein Slice-Test mit
`@WebMvcTest` und ein Integrationstest mit `@SpringBootTest` gegen einen
echten HTTP-Server.

Die Besonderheit dieses Moduls: `-start` und `-finished` enthalten
**denselben, vollstaendigen Produktivcode**. Der einzige Unterschied ist
`src/test/java` — in der `-start`-Variante leer, in der
`-finished`-Variante mit den drei Testklassen. Live gebaut wird hier
nicht die Anwendung, sondern ihre Tests.

## Der Produktivcode

Die Fachlogik steckt im `BookService`: `totalFor(isbn, quantity)`
berechnet den Gesamtpreis fuer eine Stueckzahl — **ab fuenf Exemplaren
gibt es zehn Prozent Rabatt**, kaufmaennisch auf zwei Nachkommastellen
gerundet. Der Controller reduziert den REST-Vertrag aus Modul 02 auf
drei Endpoints:

| Endpoint | Verhalten |
| --- | --- |
| `GET /api/books/{isbn}` | 200 mit dem Buch als JSON; unbekannte ISBN → 404 ueber den `RestExceptionHandler` |
| `GET /api/books/{isbn}/total?quantity=n` | 200 mit dem Gesamtpreis inklusive Mengenrabatt |
| `POST /api/books` | 201 mit Location-Header, das Buch landet in der Datenbank |

## Die drei Testarten

| Testart | Testklasse | Werkzeug | Was laeuft |
| --- | --- | --- | --- |
| Unit | `BookServiceTest` | `@ExtendWith(MockitoExtension.class)` | Kein Spring-Kontext — das Repository ist ein Mockito-Mock, fertig in Millisekunden |
| Slice | `BookControllerWebMvcTest` | `@WebMvcTest` + `MockMvc` | Nur die MVC-Schicht (Controller, Advice, JSON); der Service ist ein `@MockitoBean` |
| Integration | `BookstoreIntegrationTest` | `@SpringBootTest(RANDOM_PORT)` + `TestRestTemplate` | Die komplette Anwendung mit Tomcat und H2 — echte HTTP-Requests, nichts gemockt |

Zwei Spring-Boot-4-Fallstricke, die beim Live-Coding auffallen:
`@MockBean` gibt es nicht mehr, der Nachfolger heisst `@MockitoBean`
(aus `org.springframework.test.context.bean.override.mockito`). Und die
Test-Unterstuetzung ist in eigene Starter aufgeteilt —
`spring-boot-starter-webmvc-test` fuer `@WebMvcTest`,
`spring-boot-starter-restclient-test` fuer das `TestRestTemplate`
(siehe `pom.xml`).

## Varianten

| Modul | Inhalt |
| --- | --- |
| `sb-basics-testing-demo-start` | Vollstaendiger Produktivcode, `src/test/java` leer — die Tests entstehen live. Alle Test-Abhaengigkeiten stehen schon in der `pom.xml`. |
| `sb-basics-testing-demo-finished` | Derselbe Produktivcode plus die drei Testklassen. |

## Ablauf der Live-Demo

Ausgangspunkt ist die `-start`-Variante. Nach jedem Schritt laeuft
`mvn test` — erst mit `Tests run: 0`, am Ende mit sechs gruenen Tests.

1. **Unit-Test:** `BookServiceTest` schreiben — `@Mock BookRepository`,
   `@InjectMocks BookService`, dann die Rabattregel genau an ihrer
   Grenze testen: fuenf Exemplare zu 100.00 kosten 450.00, vier kosten
   400.00. Der zweite Fall ist kein Fleiss, sondern Praezision: Nur das
   Paar aus beiden Grenzfaellen unterscheidet `>=` von `>` — wer mag,
   aendert die Bedingung im Service kurz auf `>` und sieht den ersten
   Test rot werden. Dazu `verify(...)`: Mockito prueft nicht nur
   Rueckgabewerte, sondern auch, *wie* mit dem Mock gesprochen wurde.
2. **Slice-Test:** `BookControllerWebMvcTest` mit
   `@WebMvcTest(BookController.class)` — Spring startet nur die
   MVC-Schicht, der Service wird per `@MockitoBean` ersetzt. Der erste
   Test prueft die JSON-Antwort per `jsonPath`, der zweite laesst das
   Mock eine `BookNotFoundException` werfen und prueft, dass der
   `RestExceptionHandler` daraus 404 macht — der Handler laeuft im
   Slice mit, das Repository nicht.
3. **Integrationstest:** `BookstoreIntegrationTest` mit
   `@SpringBootTest(webEnvironment = RANDOM_PORT)` — jetzt startet die
   ganze Anwendung samt H2 auf einem freien Port. Das
   `TestRestTemplate` legt per POST ein Buch an und liest es per GET
   zurueck (die von der Datenbank vergebene Id beweist das Speichern);
   der zweite Test prueft die Rabattregel Ende-zu-Ende ueber HTTP.
4. **Abschluss:** Testpyramide an der Laufzeit ablesen — die
   Surefire-Ausgabe zeigt: Unit-Tests in Millisekunden, der Slice-Test
   braucht einen kleinen Kontext, der Integrationstest ist mit Abstand
   am teuersten. Viel unten, wenig oben.

Als Uebung fuer danach: Welchen Status liefert ein POST mit leerem
Titel? Erst eine Vermutung aufstellen, dann den Slice- oder
Integrationstest schreiben, der sie beweist.

## Starten und Testen

Die Anwendung selbst startet in beiden Varianten (der Katalog ist
anfangs leer):

```bash
cd sb-basics-testing-demo-finished
mvn spring-boot:run
```

Die Tests:

```bash
cd sb-basics-testing-demo-start
mvn test    # BUILD SUCCESS, aber noch keine Tests

cd ../sb-basics-testing-demo-finished
mvn test    # 6 Tests: 2 Unit, 2 Slice, 2 Integration
```

Jede Verhaltensbehauptung ist durch einen Test abgesichert:

| Behauptung | Test |
| --- | --- |
| Ab fuenf Exemplaren gibt es zehn Prozent Rabatt (fuenfmal 100.00 → 450.00) | `BookServiceTest.appliesBulkDiscount` |
| Unter fuenf Exemplaren gibt es keinen Rabatt (viermal 100.00 → 400.00) — die Grenze ist `>=` | `BookServiceTest.appliesNoDiscountBelowThreshold` |
| Der Service fragt das Repository genau einmal mit der ISBN | `BookServiceTest.appliesNoDiscountBelowThreshold` (`verify`) |
| `GET /api/books/{isbn}` liefert 200 und das Buch als JSON | `BookControllerWebMvcTest.getReturnsBookAsJson` |
| Unbekannte ISBN → 404 mit Fehlermeldung im Body | `BookControllerWebMvcTest.unknownIsbnReturnsNotFound` |
| `POST /api/books` liefert 201 mit Location, das Buch ist danach per GET aus der Datenbank lesbar | `BookstoreIntegrationTest.createdBookCanBeReadBack` |
| Die Rabattregel wirkt Ende-zu-Ende ueber HTTP | `BookstoreIntegrationTest.totalAppliesDiscountEndToEnd` |
