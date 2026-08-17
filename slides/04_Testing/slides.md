---
marp: true
theme: default
header: Spring Boot Basics
footer: Alexander Erben
paginate: true
---
<!-- Dichte-Stufen gegen Folienueberlauf, siehe tools/check-slide-overflow.mjs -->
<style>
section.dense { font-size: 24.5px; }
section.denser { font-size: 21px; }
section.densest { font-size: 17.5px; }
</style>

# Testen mit JUnit 5

---

## In diesem Modul

* Warum automatisiert testen und was ein guter Test uns bringt
* JUnit 5: `@Test`, Assertions, AssertJ, Lifecycle
* Unit-Tests mit Mockito: `@Mock`, `when`, `verify`
* Spring-Tests: `@SpringBootTest` und Test-Slices, `@MockitoBean`

---

## Warum automatisiert testen?

Manuelles Testen skaliert nicht: Nach jeder Änderung alle Endpoints von Hand durchklicken ist zu aufwendig. Automatisierte Tests bringen:

* **Regressionsschutz:** Ein Test, der einmal grün war und rot wird, zeigt exakt, welche Zusicherung gebrochen wurde
* **Mut zum Refactoring:** Wer eine Testsuite hat, kann umbauen. Wer keine hat, lässt lieber alles, wie es ist.
* **Ausführbare Dokumentation:** Die Namen von Testmethoden helfen dabei, den Code zu verstehen

---

<!-- _class: dense -->
## Die Testpyramide in einem Satz

**Viele schnelle, kleine Tests unten und wenige langsame, große Tests oben.**

| Ebene       | Werkzeug                      | Startet                       | Laufzeit            |
|-------------|-------------------------------|-------------------------------|---------------------|
| Unit        | JUnit + Mockito               | nichts, nur die Klasse        | Millisekunden       |
| Slice       | `@WebMvcTest`, `@DataJpaTest` | einen Ausschnitt des Kontexts | unter einer Sekunde |
| Integration | `@SpringBootTest`             | die ganze Anwendung           | Sekunden            |

* Je höher, desto realistischer und leider auch teurer, langsamer, brüchiger, schwerer zu diagnostizieren.
* Die Konsequenz: Fachlogik unten absichern und oben nur noch prüfen, dass die Teile zusammenspielen.

---

# JUnit 5 Grundlagen

---

<!-- _class: denser -->
## @Test und der erste Test

Tests liegen in `src/test/java`, im selben Package wie der Produktivcode. Eine Testmethode ist eine Methode mit `@Test`.

```java
class BookServiceTest {

    @Test
    void appliesBulkDiscount() {
        // Arrange: Ausgangslage aufbauen
        // Act:     die eine getestete Operation ausführen
        // Assert:  das Ergebnis prüfen
    }
}
```

* `mvn test` und jede IDE finden Tests automatisch dank Konvention statt Konfiguration
* Das Muster **Arrange — Act — Assert** strukturiert jeden Test: erst Ausgangslage, dann eine Aktion, dann die Prüfung.
* JUnit 5 wird mit Mockito und AssertJ im `spring-boot-starter-test` mit eingebunden

---

<!-- _class: dense -->
## Assertions

JUnit bringt statische Prüfmethoden mit: `Assertions.assertEquals`, `assertTrue`, `assertNotNull`:

```java
assertEquals(new BigDecimal("450.00"), total);
```

* Schlägt eine Assertion fehl, wirft sie einen Fehler und der Test ist rot, die restlichen Zeilen laufen nicht mehr.
* Reihenfolge bei `assertEquals`: **erst erwartet, dann tatsächlich**. Vertauscht ergibt sie irreführende Fehlermeldungen.
* Im Beispiel ist ein klassischer Fehler: `new BigDecimal("450.0")` und `new BigDecimal("450.00")` sind für `equals` **verschieden** (gleicher Wert, andere Skala). `assertEquals` schlägt fehl, obwohl der Betrag stimmt.

---

## AssertJ: fluent assertions

Deshalb benutzen wir durchgehend **AssertJ** (auch im `spring-boot-starter-test` enthalten):

```java
import static org.assertj.core.api.Assertions.assertThat;

assertThat(total).isEqualByComparingTo(new BigDecimal("450.00"));
assertThat(fetched.getBody().getTitle()).isEqualTo("Refactoring");
assertThat(fetched.getBody().getId()).isNotNull();
```

* `isEqualByComparingTo` vergleicht `BigDecimal` **nach Wert**
* Die Prüfungen lesen sich wie ein Satz, die Fehlermeldungen zeigen erwarteten und tatsächlichen Wert gut formatiert

---

<!-- _class: denser -->
## @BeforeEach und @AfterEach

JUnit erzeugt **für jede Testmethode eine neue Instanz** der Testklasse. Tests können sich nicht über Felder gegenseitig beeinflussen. Gemeinsame Ausgangslage gehört in eine Lifecycle-Methode:

```java
private Book book;

@BeforeEach
void setUp() {
    book = new Book();
    book.setIsbn("978-0-13-468599-1");
    book.setNetPrice(new BigDecimal("100.00"));
}
```

* `@BeforeEach` läuft vor jedem Test, `@AfterEach` danach (Aufräumen — selten nötig); `@BeforeAll`/`@AfterAll` laufen einmal pro Klasse und müssen `static` sein.
* Faustregel: So wenig wie möglich in `setUp`. Was nur ein Test braucht, steht in diesem Test. Ein Test soll ohne Scrollen lesbar sein.

---

## @DisplayName

Der Methodenname ist ein Bezeichner, die fachliche Aussage darf ein Satz sein:

```java
@Test
@DisplayName("Ab fünf Exemplaren gibt es zehn Prozent Rabatt")
void appliesBulkDiscount() { ... }
```

* IDE und Surefire-Report zeigen den Satz statt des Methodennamens. Ein roter Test benennt dann direkt die gebrochene **Regel**.
* Gut investiert bei fachlichen Regeln und Grenzfällen; bei trivialen Tests reicht ein sprechender Methodenname.

---

<!-- _class: dense -->
## Erwartete Exceptions

Exceptions sind Teil des Contracts: Eine unbekannte ISBN **muss** eine `BookNotFoundException` auslösen. AssertJ prüft das mit `assertThatThrownBy`:

```java
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Test
void unknownIsbnThrows() {
    assertThatThrownBy(() -> bookService.findByIsbn("999-does-not-exist"))
            .isInstanceOf(BookNotFoundException.class)
            .hasMessageContaining("999-does-not-exist");
}
```

* Das Lambda kapselt den Aufruf: fliegt **keine** Exception, ist der Test rot.
* JUnit-Alternative: `assertThrows(BookNotFoundException.class, () -> ...)` gibt die Exception zurück, die Prüfung der Message ist dann Handarbeit.

---

# Unit-Tests mit Mockito

---

<!-- _class: dense -->
## Warum Mocks?

Die Rabattregel im `BookService` ist pure Fachlogik, aber `totalFor` ruft `bookRepository.findByIsbn(...)` auf, wozu die Datenbank erreichbar sein muss.

```java
public BigDecimal totalFor(String isbn, int quantity) {
    Book book = findByIsbn(isbn);   // -> bookRepository.findByIsbn(isbn)
    ...
}
```

* Für einen Test der **Rechenregel** ist die Datenbank unnötiger Ballast
* Ein **Mock** ersetzt die Abhängigkeit durch ein programmierbares Double: "Wenn dich jemand nach dieser ISBN fragt, gib dieses Buch zurück."
* Das lässt den Test einerseits nur noch eine Sache prüfen, aber andererseits prüft er damit nicht mehr die Integration.

---

<!-- _class: denser -->
## @Mock und @InjectMocks

Mockito integriert sich über eine JUnit-Extension:

```java
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;
}
```

* `@ExtendWith(MockitoExtension.class)` aktiviert Mockito für diese Klasse. Es entsteht **kein** Spring-Kontext.
* `@Mock` erzeugt ein Mock-Objekt des Interfaces `BookRepository` und jede Methode gibt erst einmal `null`, `0` oder leere Werte zurück.
* `@InjectMocks` instanziiert den echten `BookService` und reicht die Mocks in seinen Konstruktor

---

<!-- _class: denser -->
## when und thenReturn

Verhalten bekommt das Mock per so genanntem **Stubbing**:

```java
@Test
@DisplayName("Ab fünf Exemplaren gibt es zehn Prozent Rabatt")
void appliesBulkDiscount() {
    Book book = new Book();
    book.setIsbn("978-0-13-468599-1");
    book.setNetPrice(new BigDecimal("100.00"));
    when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(book));

    BigDecimal total = bookService.totalFor("978-0-13-468599-1", 5);

    assertThat(total).isEqualByComparingTo(new BigDecimal("450.00"));
}
```

* `when(...).thenReturn(...)` liest sich als Regel: "Wenn `findByIsbn` mit irgendeinem String aufgerufen wird, gib dieses Buch zurück."
* `anyString()` ist ein sog. **Argument-Matcher**

---

## verify

Mockito kann auch prüfen, **wie** mit dem Mock gesprochen wurde:

```java
BigDecimal total = bookService.totalFor("978-0-13-468599-1", 4);

assertThat(total).isEqualByComparingTo(new BigDecimal("400.00"));
verify(bookRepository, times(1)).findByIsbn("978-0-13-468599-1");
```

* `verify` schlägt fehl, wenn der Aufruf nicht (oder zu oft, oder mit anderen Argumenten) stattfand.
* Sinnvoll, wenn der Aufruf selbst der beobachtbare Effekt ist
* Sparsam einsetzen: Wer jede Interaktion verifiziert, koppelt die Tests zu stark an die Implementierung

---

## Wann NICHT mocken

Der häufigste Anfängerfehler ist nicht zu wenig Mocking, es ist *zu viel*:

* **Keine Datenobjekte mocken.** Ein `Book` baut man mit `new Book()` und Settern. Win Mock mit fünf gestubbten Gettern ist länger, brüchiger und testet nichts.
* **Nicht die Logik der Abhängigkeit im Mock nachbauen.**
* **Nicht mocken, was man in Millisekunden echt haben kann.** Reine Hilfsklassen ohne I/O einfach benutzen.
* **Framework-Interna nicht mocken.** Wer `JpaRepository`-Details wie das Speicherverhalten nachstellt, testet eine Vermutung über Spring Data. Dafür gibt es `@DataJpaTest` (gleich).

---

# Spring-Tests

---

<!-- _class: denser -->
## @SpringBootTest mit dem vollen Kontext

Manche Testfälle kann man mit einem Unit Test nicht abdecken. Dafür startet `@SpringBootTest` die **ganze Anwendung**:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class BookstoreIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
}
```

* `RANDOM_PORT`: echter Tomcat auf einem freien Port: kein Konflikt mit einer laufenden Instanz oder parallelen Builds.
* Das `TestRestTemplate` schickt **echte HTTP-Requests** gegen diesen Port; die Basis-URL ist vorkonfiguriert.
* Kein Mocking: Controller, Service, Hibernate, H2 ist komplett echt.

---

<!-- _class: dense -->
## Was das kostet

Tests mit Kontext sind teuer:

```text
BookServiceTest           Tests run: 2, Time elapsed: 0.108 s
BookControllerWebMvcTest  Tests run: 2, Time elapsed: 0.230 s
BookstoreIntegrationTest  Tests run: 2, Time elapsed: 1.806 s
```

* `@SpringBootTest` fährt Component Scan, Auto-Configuration, Hibernate samt Schema und Tomcat hoch. Bei einer kleinen Demo sind das Sekunden, bei einer gewachsenen Anwendung **deutlich mehr**.
* Spring mitigiert dies mit **Context-Caching**: Testklassen mit identischer Konfiguration teilen sich einen einmal gestarteten Kontext. Aber jede Abweichung, z.B. andere Properties, ein anderes `@MockitoBean`, erzwingt einen **neuen** Kontext.
* Und: Der geteilte Kontext teilt auch die Datenbank.

Deswegen sind die **Test-Slices** entstanden.

---

<!-- _class: densest -->
## Test Slices

Ein Slice startet nicht die ganze Anwendung, sondern eine Schicht mit den Beans und der Auto-Configuration, die diese Schicht braucht:

| Slice          | Startet                                 | Typische Frage                         |
|----------------|-----------------------------------------|----------------------------------------|
| `@WebMvcTest`  | Controller, Advice, JSON-Serialisierung | Stimmen Statuscode und Response-Body?  |
| `@DataJpaTest` | Entities, Repositories, H2              | Findet die Derived Query das Richtige? |
| `@JsonTest`    | nur die JSON-Schicht                    | Wird das Datum richtig serialisiert?   |

* Alles außerhalb des Slices existiert nicht. Was der Controller braucht, wird gemockt (gleich: `@MockitoBean`).
* Deutlich schneller als der volle Kontext und **fokussiert**: Schlägt ein `@WebMvcTest` fehl, liegt es an der Web-Schicht.
* Seit Boot 4 liegen die Slices in eigenen Test-Startern: `spring-boot-starter-webmvc-test` für `@WebMvcTest`, `spring-boot-starter-restclient-test` für das `TestRestTemplate`. Dessen Basis (`RestTemplateBuilder`) steckt zusätzlich in `spring-boot-starter-restclient`. Alle drei stehen im Test-Scope in der `pom.xml` der Demo.

---

<!-- _class: densest -->
## @WebMvcTest und MockMvc

Der Slice-Test der Demo prüft den REST-Vertrag aus Modul 02 ohne Server und ohne Datenbank:

```java
@WebMvcTest(BookController.class)
class BookControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getReturnsBookAsJson() throws Exception {
        mockMvc.perform(get("/api/books/978-0-13-468599-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Effective Java"));
    }
}
```

* `@WebMvcTest(BookController.class)`: nur dieser Controller, dazu Advice-Klassen und JSON
* `MockMvc` schickt Requests durch den echten `DispatcherServlet`-Stack, aber **ohne echten HTTP-Server**
* `jsonPath("$.title")` navigiert die JSON-Antwort; der zweite Demo-Test prüft so, dass der `RestExceptionHandler` aus einer `BookNotFoundException` eine 404 mit Fehlermeldung macht

---

## @MockitoBean

Der Controller braucht einen `BookService`, aber im Slice existiert keiner. `@MockitoBean` legt ein Mockito-Mock als Bean in den Test-Kontext:

```java
@MockitoBean
private BookService bookService;

@Test
void unknownIsbnReturnsNotFound() throws Exception {
    when(bookService.findByIsbn("999-does-not-exist"))
            .thenThrow(new BookNotFoundException("999-does-not-exist"));

    mockMvc.perform(get("/api/books/999-does-not-exist"))
            .andExpect(status().isNotFound());
}
```

---

<!-- _class: densest -->
## @DataJpaTest

Das Gegenstück für die Persistenzschicht ist `@DataJpaTest` und enthält nur Entities, Repositories und eine In-Memory-Datenbank.

```java
@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void findsByIsbn() {
        // Testdaten anlegen, per findByIsbn wiederfinden
    }
}
```

* Sinnvoll für alles, was ein Mock nicht beweisen kann:, z.B. stimmt das Entity-Mapping? Liefert die Derived Query `findByIsbn` das Richtige?
* Jeder Test läuft in einer Transaktion, die am Ende zurückgerollt wird. Die Tests hinterlassen einander keine Daten.
* Web-Schicht und Services existieren in diesem Slice nicht.

---

# Demo

---

## Was wir gleich schreiben

| Schritt | Testklasse                 | Ziel                                                           |
|---------|----------------------------|----------------------------------------------------------------|
| 1       | `BookServiceTest`          | Beide Grenzfälle der Rabattregel                               |
| 2       | `BookControllerWebMvcTest` | 404 aus dem `RestExceptionHandler`, ohne Service und Datenbank |
| 3       | `BookstoreIntegrationTest` | POST, dann GET, die von H2 vergebene Id beweist das Speichern  |
| 4       | Surefire-Ausgabe           | Die Testpyramide, ablesbar an der Laufzeit                     |

---

# Übung

---

<!-- _class: denser -->
## Assignment 04

`assignments/sb-basics-testing-assignment` enthält wieder die **Kursverwaltung** aber diesmal ist der Produktivcode komplett fertig und **ihr schreibt die Tests.**

* Gegeben: `ParticipantService` mit `register(courseCode, email)`. Er meldet an, solange freie Plätze da sind, ansonsten wird `false` zurückgegeben. Dazu ein Controller (`201` bei Erfolg, `409` bei ausgebuchtem Kurs), Entities und Repositories. `src/test/java` ist leer.
* Drei Aufgaben: Unit-Test für `register` mit `@Mock`/`@InjectMocks`, `@WebMvcTest` mit `@MockitoBean`, `@SpringBootTest` mit `TestRestTemplate`.

Das Erfolgskriterium ist eine **Mutationsprobe**: Ändert im Produktivcode die Platz-Bedingung von `<` auf `<=` und mindestens einer eurer Tests muss rot werden. Dasselbe mit dem Statuscode `409` → `400`. Bleibt alles grün, testet eure Suite die Regel nicht und es fehlt ein Test.

Musterlösung: `solutions/sb-basics-testing-solution`.
