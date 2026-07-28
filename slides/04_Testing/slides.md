---
marp: true
theme: default
header: Spring Boot Basics
footer: Alexander Erben
paginate: true
---

# Testen mit JUnit 5

---

## In diesem Modul

* Warum automatisiert testen — und was ein guter Test leistet
* JUnit 5: `@Test`, Assertions, AssertJ, Lifecycle
* Unit-Tests mit Mockito: `@Mock`, `when`, `verify` — und wann man **nicht** mockt
* Spring-Tests: `@SpringBootTest`, was der volle Kontext kostet, Test-Slices
* `@MockitoBean` — und warum `@MockBean` aus alten Beispielen nicht mehr kompiliert
* Demo: die Buchhandlung bekommt Tests — Kurs-Stand: **Spring Boot 4.0.2, Java 21**

Modul 03 hat es angekündigt: Heute testen wir die Buchhandlung durch alle Schichten. Der Produktivcode ist der aus Modul 02 und 03 — neu ist nur `src/test/java`.

---

## Warum automatisiert testen?

Manuelles Testen skaliert nicht: Nach jeder Änderung alle Endpoints von Hand durchklicken — das macht niemand, und genau dann schleichen sich Regressionen ein.

* **Regressionsschutz:** Ein Test, der einmal grün war und rot wird, zeigt exakt, welche Zusicherung gebrochen wurde — Minuten nach der Änderung, nicht Wochen später in Produktion.
* **Mut zum Refactoring:** Wer eine Testsuite hat, kann umbauen. Wer keine hat, lässt lieber alles, wie es ist — auch das Schlechte.
* **Ausführbare Dokumentation:** `appliesBulkDiscount` sagt präziser als jedes Wiki, was die Rabattregel tut. Tests lügen nicht — sie laufen.
* **Schnelles Feedback:** `mvn test` in Sekunden statt Deployment und Klicktour.

Ein guter Test prüft **Verhalten** („ab fünf Exemplaren zehn Prozent Rabatt"), nicht Implementierung („ruft intern `multiply` auf"). Das zieht sich durch das ganze Modul.

---

## Die Testpyramide in einem Satz

**Viele schnelle, kleine Tests unten — wenige langsame, große Tests oben.**

| Ebene | Werkzeug | Startet | Laufzeit |
| --- | --- | --- | --- |
| Unit | JUnit + Mockito | nichts — nur die Klasse | Millisekunden |
| Slice | `@WebMvcTest`, `@DataJpaTest` | einen Ausschnitt des Kontexts | unter einer Sekunde |
| Integration | `@SpringBootTest` | die ganze Anwendung | Sekunden |

* Je höher, desto realistischer — und desto teurer: langsamer, brüchiger, schwerer zu diagnostizieren.
* Die Konsequenz: Fachlogik unten absichern, oben nur noch prüfen, dass die Teile zusammenspielen.
* Wie die Ebenen konkret aussehen, ist der Rest dieses Moduls.

---

# JUnit 5 Grundlagen

---

## @Test und der erste Test

Tests liegen in `src/test/java`, im selben Package wie der Produktivcode. Eine Testmethode ist eine Methode mit `@Test` — kein `public` nötig, kein Naming-Zwang:

```java
class BookServiceTest {

    @Test
    void appliesBulkDiscount() {
        // Arrange: Ausgangslage aufbauen
        // Act:     die eine getestete Operation ausfuehren
        // Assert:  das Ergebnis pruefen
    }
}
```

* `mvn test` und jede IDE finden Tests automatisch — Konvention statt Konfiguration, wie überall in Boot.
* Das Muster **Arrange — Act — Assert** strukturiert jeden Test: erst Ausgangslage, dann genau eine Aktion, dann die Prüfung.
* JUnit 5 steckt (mit Mockito und AssertJ) im `spring-boot-starter-test` — den bringt jedes Initializr-Projekt mit.

---

## Assertions

JUnit bringt statische Prüfmethoden mit — `Assertions.assertEquals`, `assertTrue`, `assertNotNull`:

```java
assertEquals(new BigDecimal("450.00"), total);
```

* Schlägt eine Assertion fehl, wirft sie einen Fehler — der Test ist rot, die restlichen Zeilen laufen nicht mehr.
* Reihenfolge bei `assertEquals`: **erst erwartet, dann tatsächlich** — vertauscht ergibt sie irreführende Fehlermeldungen.
* Und hier lauert schon ein Klassiker: `new BigDecimal("450.0")` und `new BigDecimal("450.00")` sind für `equals` **verschieden** (gleicher Wert, andere Skala) — `assertEquals` schlägt fehl, obwohl der Betrag stimmt.

---

## AssertJ: fluent assertions

Deshalb benutzen wir durchgehend **AssertJ** (auch im `spring-boot-starter-test` enthalten): ein Einstiegspunkt `assertThat`, danach führt die Autovervollständigung:

```java
import static org.assertj.core.api.Assertions.assertThat;

assertThat(total).isEqualByComparingTo(new BigDecimal("450.00"));
assertThat(fetched.getBody().getTitle()).isEqualTo("Refactoring");
assertThat(fetched.getBody().getId()).isNotNull();
```

* `isEqualByComparingTo` vergleicht `BigDecimal` **nach Wert** — das Skala-Problem von eben ist weg.
* Die Prüfungen lesen sich wie ein Satz, die Fehlermeldungen zeigen erwarteten und tatsächlichen Wert sauber formatiert — und es gibt spezialisierte Assertions für Collections, Strings, Optionals und Exceptions.

---

## @BeforeEach und @AfterEach

JUnit erzeugt **für jede Testmethode eine neue Instanz** der Testklasse — Tests können sich nicht über Felder gegenseitig beeinflussen. Gemeinsame Ausgangslage gehört in eine Lifecycle-Methode:

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
* Faustregel: So wenig wie möglich in `setUp` — was nur ein Test braucht, steht in diesem Test. Ein Test soll ohne Scrollen lesbar sein.

---

## @DisplayName

Der Methodenname ist ein Bezeichner — die fachliche Aussage darf ein Satz sein:

```java
@Test
@DisplayName("Ab fuenf Exemplaren gibt es zehn Prozent Rabatt")
void appliesBulkDiscount() { ... }
```

* IDE und Surefire-Report zeigen den Satz statt des Methodennamens — ein roter Test benennt dann direkt die gebrochene **Regel**.
* Gut investiert bei fachlichen Regeln und Grenzfällen; bei trivialen Tests reicht ein sprechender Methodenname.

---

## Erwartete Exceptions

Auch das Fehlverhalten ist Vertrag: Eine unbekannte ISBN **muss** eine `BookNotFoundException` auslösen. AssertJ prüft das mit `assertThatThrownBy`:

```java
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Test
void unknownIsbnThrows() {
    assertThatThrownBy(() -> bookService.findByIsbn("999-does-not-exist"))
            .isInstanceOf(BookNotFoundException.class)
            .hasMessageContaining("999-does-not-exist");
}
```

* Das Lambda kapselt den Aufruf — fliegt **keine** Exception, ist der Test rot.
* JUnit-Alternative: `assertThrows(BookNotFoundException.class, () -> ...)` gibt die Exception zurück, die Prüfung der Message ist dann Handarbeit.
* Nie mit `try`/`catch` selbst basteln: Der Klassiker ist ein vergessenes `fail()` im `try` — der Test bleibt grün, obwohl nichts geworfen wurde.

---

# Unit-Tests mit Mockito

---

## Warum Mocks?

Die Rabattregel im `BookService` ist pure Fachlogik — aber `totalFor` ruft `bookRepository.findByIsbn(...)` auf, und hinter dem Repository stehen Hibernate und eine Datenbank:

```java
public BigDecimal totalFor(String isbn, int quantity) {
    Book book = findByIsbn(isbn);   // -> bookRepository.findByIsbn(isbn)
    ...
}
```

* Für einen Test der **Rechenregel** ist die Datenbank Ballast: langsam, Zustand muss vorbereitet werden, Fehlerursachen vermischen sich.
* Ein **Mock** ersetzt die Abhängigkeit durch ein programmierbares Double: „Wenn dich jemand nach dieser ISBN fragt, gib dieses Buch zurück."
* Der Test kontrolliert damit die komplette Umgebung der Klasse — und prüft nur noch ihre eigene Logik. Das ist der Unit-Test: kein Spring, keine Datenbank, Millisekunden.

---

## @Mock und @InjectMocks

Mockito integriert sich über eine JUnit-Extension — so beginnt der `BookServiceTest` der Demo:

```java
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;
}
```

* `@ExtendWith(MockitoExtension.class)` aktiviert Mockito für diese Klasse — **kein** Spring-Kontext, hier startet gar nichts.
* `@Mock` erzeugt ein Mock-Objekt des Interfaces `BookRepository` — jede Methode gibt erst einmal `null`, `0` oder leere Werte zurück.
* `@InjectMocks` instanziiert den echten `BookService` und reicht die Mocks in seinen Konstruktor — Constructor Injection (Modul 00) zahlt sich hier aus: Die Klasse ist ohne Spring konstruierbar.

---

## when und thenReturn

Verhalten bekommt das Mock per **Stubbing** — so testet die Demo die Rabattgrenze:

```java
@Test
@DisplayName("Ab fuenf Exemplaren gibt es zehn Prozent Rabatt")
void appliesBulkDiscount() {
    Book book = new Book();
    book.setIsbn("978-0-13-468599-1");
    book.setNetPrice(new BigDecimal("100.00"));
    when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(book));

    BigDecimal total = bookService.totalFor("978-0-13-468599-1", 5);

    assertThat(total).isEqualByComparingTo(new BigDecimal("450.00"));
}
```

* `when(...).thenReturn(...)` liest sich als Regel: „Wenn `findByIsbn` mit irgendeinem String aufgerufen wird, gib dieses Buch zurück."
* `anyString()` ist ein **Argument-Matcher** — alternativ bindet ein konkreter Wert (`findByIsbn("978-...")`) die Regel an genau dieses Argument.
* Der Zwillingstest `appliesNoDiscountBelowThreshold` prüft **vier** Exemplare → `400.00`. Erst das Paar beider Grenzfälle unterscheidet `>=` von `>` — ein Grenzfall allein tut es nicht.

---

## verify

Mockito kann auch prüfen, **wie** mit dem Mock gesprochen wurde:

```java
BigDecimal total = bookService.totalFor("978-0-13-468599-1", 4);

assertThat(total).isEqualByComparingTo(new BigDecimal("400.00"));
verify(bookRepository, times(1)).findByIsbn("978-0-13-468599-1");
```

* `verify` schlägt fehl, wenn der Aufruf nicht (oder zu oft, oder mit anderen Argumenten) stattfand.
* Sinnvoll, wenn der Aufruf selbst der beobachtbare Effekt ist — eine E-Mail versenden, ein Event publizieren: Da gibt es keinen Rückgabewert zu prüfen.
* Aber sparsam einsetzen: Wer jede Interaktion verifiziert, schweißt den Test an die Implementierung — nach jedem Refactoring werden Tests rot, obwohl das Verhalten stimmt.

---

## Wann NICHT mocken

Der häufigste Anfängerfehler ist nicht zu wenig Mocking — es ist zu viel:

* **Keine Datenobjekte mocken.** Ein `Book` baut man mit `new Book()` und Settern — ein Mock mit fünf gestubbten Gettern ist länger, brüchiger und testet nichts.
* **Nicht die Logik der Abhängigkeit im Mock nachbauen.** Wer im Stub die Rabattrechnung nachprogrammiert, testet am Ende, ob der Mock rechnen kann — der Test wird grün, egal was der Service tut.
* **Nicht mocken, was man in Millisekunden echt haben kann.** Reine Hilfsklassen ohne I/O einfach benutzen.
* **Framework-Interna nicht mocken.** Wer `JpaRepository`-Feinheiten wie das Speicherverhalten nachstellt, testet eine Vermutung über Spring Data — dafür gibt es `@DataJpaTest` (gleich).

Faustregel: **Gemockt wird die Grenze** — Datenbank, HTTP, Uhrzeit, Zufall. Geprüft wird das **Ergebnis**, nicht der Weg dorthin. Ein Mock, der nur die Implementierung spiegelt, ist ein Test, der nie wieder etwas findet.

---

# Spring-Tests

---

## @SpringBootTest: der volle Kontext

Manche Fragen kann kein Unit-Test beantworten: Landet ein per POST angelegtes Buch wirklich in der Datenbank? Dafür startet `@SpringBootTest` die **ganze Anwendung**:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class BookstoreIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
}
```

* `RANDOM_PORT`: echter Tomcat auf einem freien Port — kein Konflikt mit einer laufenden Instanz oder parallelen Builds.
* Das `TestRestTemplate` schickt **echte HTTP-Requests** gegen diesen Port; die Basis-URL ist vorkonfiguriert, `restTemplate.getForEntity("/api/books/...", Book.class)` genügt.
* Hier ist nichts gemockt: Controller, Service, Hibernate, H2 — alles echt. Der Test aus der Demo legt per POST ein Buch an und beweist mit der von der Datenbank vergebenen Id, dass es wirklich gespeichert wurde.

---

## Was das kostet

Der volle Kontext ist der teuerste Test im Baukasten — die Surefire-Ausgabe der Demo zeigt es:

```text
BookServiceTest           Tests run: 2, Time elapsed: 0.108 s
BookControllerWebMvcTest  Tests run: 2, Time elapsed: 0.230 s
BookstoreIntegrationTest  Tests run: 2, Time elapsed: 1.806 s
```

* `@SpringBootTest` fährt Component Scan, Auto-Configuration, Hibernate samt Schema und Tomcat hoch — bei einer kleinen Demo Sekunden, bei einer gewachsenen Anwendung **deutlich mehr**. Mal hundert Tests gerechnet wird aus der Testsuite eine Kaffeepause.
* Spring dämpft das mit **Context-Caching**: Testklassen mit identischer Konfiguration teilen sich einen einmal gestarteten Kontext. Aber jede Abweichung — andere Properties, ein anderes `@MockitoBean` — erzwingt einen **neuen** Kontext.
* Und: Der geteilte Kontext teilt auch die Datenbank. Die Demo-Tests benutzen deshalb je eigene ISBNs, damit sie unabhängig von ihrer Reihenfolge bleiben.

Genau aus diesen Kosten sind die **Test-Slices** entstanden.

---

## Test Slices

Ein Slice startet nicht die ganze Anwendung, sondern **eine Schicht** — mit genau den Beans und der Auto-Configuration, die diese Schicht braucht:

| Slice | Startet | Typische Frage |
| --- | --- | --- |
| `@WebMvcTest` | Controller, Advice, JSON-Serialisierung | Stimmen Statuscode und Response-Body? |
| `@DataJpaTest` | Entities, Repositories, H2 | Findet die Derived Query das Richtige? |
| `@JsonTest` | nur die JSON-Schicht | Wird das Datum richtig serialisiert? |

* Alles außerhalb des Slices existiert nicht — was der Controller braucht, wird gemockt (gleich: `@MockitoBean`).
* Deutlich schneller als der volle Kontext, und **fokussiert**: Schlägt ein `@WebMvcTest` fehl, liegt es an der Web-Schicht — nicht irgendwo.
* Seit Boot 4 liegen die Slices in eigenen Test-Startern: `spring-boot-starter-webmvc-test` für `@WebMvcTest`, `spring-boot-starter-restclient-test` für das `TestRestTemplate` — dessen Basis (`RestTemplateBuilder`) steckt zusätzlich in `spring-boot-starter-restclient`. Alle drei stehen im Test-Scope in der `pom.xml` der Demo.

---

## @WebMvcTest und MockMvc

Der Slice-Test der Demo prüft den REST-Vertrag aus Modul 02 — ohne Server, ohne Datenbank:

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

* `@WebMvcTest(BookController.class)`: nur dieser Controller, dazu Advice-Klassen und JSON — kein Service, kein Repository.
* `MockMvc` schickt Requests durch den echten `DispatcherServlet`-Stack, aber **ohne HTTP-Server** — daher die Geschwindigkeit.
* `jsonPath("$.title")` navigiert die JSON-Antwort; der zweite Demo-Test prüft so, dass der `RestExceptionHandler` aus einer `BookNotFoundException` eine 404 mit Fehlermeldung macht — der Handler läuft im Slice mit.

---

## @MockitoBean

Der Controller braucht einen `BookService` — im Slice existiert keiner. `@MockitoBean` legt ein Mockito-Mock **als Bean in den Test-Kontext**:

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

* Gestubbt wird wie gewohnt mit `when(...)` — der Unterschied zu `@Mock`: Das Mock wird per Dependency Injection in den Controller gereicht, nicht per `@InjectMocks`.
* **Achtung, veraltete Vorlagen:** Ältere Beispiele im Netz und in Büchern zeigen hier `@MockBean`. Diese Annotation ist seit **Spring Boot 3.4 abgelöst** und in **Boot 4 entfernt** — kopierter `@MockBean`-Code kompiliert schlicht nicht. Das ist kein Fehler in eurem Setup, sondern im Alter der Vorlage.
* Der Nachfolger heißt `@MockitoBean` und kommt aus `org.springframework.test.context.bean.override.mockito`.

---

## @DataJpaTest

Das Gegenstück für die Persistenzschicht — nur Entities, Repositories und eine In-Memory-Datenbank. Das Beispiel stammt aus der Data-JPA-Demo von Modul 03 (nicht Teil dieser Demo):

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

* Sinnvoll für alles, was ein Mock gerade **nicht** beweisen kann: Stimmt das Entity-Mapping? Liefert die Derived Query `findByIsbn` das Richtige? Funktioniert die `@Query` aus Assignment 03?
* Jeder Test läuft in einer Transaktion, die am Ende **zurückgerollt** wird — die Tests hinterlassen einander keine Daten.
* Web-Schicht und Services existieren in diesem Slice nicht.

---

## Welchen Test wann?

Keine Aufzählung, sondern eine Entscheidungsfrage: **Was genau soll dieser Test beweisen?**

| Die Frage | Der Test |
| --- | --- |
| Rechnet die Fachlogik richtig — auch am Grenzfall? | Unit-Test mit Mockito |
| Stimmen Statuscode, JSON, Fehlerbehandlung? | `@WebMvcTest` |
| Liefern Mapping und Queries das Richtige? | `@DataJpaTest` |
| Spielen die Schichten wirklich zusammen? | wenige `@SpringBootTest` |

* Die Demo hat je **zwei** Tests pro Art — ein Demo-Artefakt, damit jede Testart einmal vorkommt, und **keine Empfehlung**, Unit-, Slice- und Integrationstests in gleicher Zahl zu schreiben. In echten Projekten gilt die Pyramide: viele Unit-Tests, gezielt Slices, eine Handvoll Integrationstests für die kritischen Pfade.
* Im Zweifel: die **billigste** Testart wählen, die die Frage beantwortet. Was ein Unit-Test beweisen kann, braucht keinen Kontext.
* Mehr Werkzeug — parametrisierte Tests, das Extension-Modell, echte Datenbanken per Testcontainers — liefert das Aufbaumodul 12.

---

# Demo

---

## Was wir gleich schreiben

`demos/sb-basics-testing-demo` — die Besonderheit: `-start` enthält den **fertigen Produktivcode** der Buchhandlung (Controller aus Modul 02, Service mit Rabattregel, Repository mit H2 aus Modul 03), aber ein leeres `src/test/java`. Live entsteht nicht die Anwendung, sondern ihre Tests:

| Schritt | Testklasse | Aha-Moment |
| --- | --- | --- |
| 1 | `BookServiceTest` | Beide Grenzfälle der Rabattregel — nur das Paar entlarvt `>` statt `>=` |
| 2 | `BookControllerWebMvcTest` | 404 aus dem `RestExceptionHandler`, ohne Service und Datenbank |
| 3 | `BookstoreIntegrationTest` | POST, dann GET — die von H2 vergebene Id beweist das Speichern |
| 4 | Surefire-Ausgabe | Die Testpyramide, ablesbar an der Laufzeit |

* Nach jedem Schritt läuft `mvn test` — erst `Tests run: 0`, am Ende sechs grüne Tests.
* Zwischendurch drehen wir im Service `>=` auf `>` und sehen den Grenzfall-Test rot werden — der Test findet den Fehler, bevor es ein Kunde tut.

---

# Übung

---

## Assignment 04

`assignments/sb-basics-testing-assignment` — wieder die **Kursverwaltung**, aber diesmal mit umgekehrtem Vertrag: Der Produktivcode ist komplett fertig, **ihr schreibt die Tests.**

* Gegeben: `ParticipantService` mit `register(courseCode, email)` — meldet an, solange freie Plätze da sind, sonst `false` — dazu Controller (`201` bei Erfolg, `409` bei ausgebuchtem Kurs), Entities und Repositories. `src/test/java` ist leer.
* Drei Aufgaben entlang der Demo: Unit-Test für `register` mit `@Mock`/`@InjectMocks`, `@WebMvcTest` mit `@MockitoBean`, `@SpringBootTest` mit `TestRestTemplate`.

Das Erfolgskriterium ist eine **Mutationsprobe**: Ändert im Produktivcode die Platz-Bedingung von `<` auf `<=` — mindestens einer eurer Tests muss rot werden. Dasselbe mit dem Statuscode `409` → `400`. Bleibt alles grün, testet eure Suite die Regel nicht — dann fehlt ein Test.

* Genau das ist der Maßstab für Tests: nicht, dass sie grün sind — sondern dass sie rot werden können.
* Musterlösung: `solutions/sb-basics-testing-solution`.
