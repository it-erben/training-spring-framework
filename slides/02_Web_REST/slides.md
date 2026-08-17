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

# RESTful Services mit Spring Boot

---

## In diesem Modul

* Was REST bedeutet: Ressourcen, Verben, Statuscodes
* Der Controller: `@RestController`, Mappings, `ResponseEntity`
* JSON mit Jackson: Records als DTOs und warum keine Entities
* Eingaben prüfen mit Bean Validation
* Fehlerbehandlung: vom `@ExceptionHandler` zum `@RestControllerAdvice`
* HTTP selbst aufrufen: `RestClient`
* Demo: die REST-Schnittstelle der Buchhandlung. Kurs-Stand: **Spring Boot
  4.0.2, Java 21**

---

<!-- _class: dense -->
## Was REST bedeutet

**RE**presentational **S**tate **T**ransfer — ein Architekturstil für
HTTP-Schnittstellen, keine Technologie:

* **Ressourcen** sind die Substantive der Fachlichkeit: ein Buch, die Liste
  aller Bücher. Jede Ressource hat eine **URL** — `/api/books`,
  `/api/books/{isbn}`.
* **HTTP-Verben** sind die Operationen: `GET` liest, `POST` legt an, `DELETE`
  löscht. Die URL bleibt dieselbe, das Verb macht hier den Unterschied.
* **Statuscodes** melden das Ergebnis: `200` für Erfolg, `404` für "gibt es
  nicht", `400` für "die Anfrage war fehlerhaft".
* **Repräsentationen:** Der Client bekommt nie das Objekt selbst, sondern eine
  Darstellung, bei uns durchgehend JSON.

---

<!-- _class: densest -->
## Ressourcen, Verben, Statuscodes

Die Buchhandlung aus den Modulen 00 und 01 bekommt nun eine REST-Schnittstelle:

| Endpunkt                   | Bedeutung         | Erfolg             | Fehler                  |
|----------------------------|-------------------|--------------------|-------------------------|
| `GET /api/books`           | Alle Bücher lesen | `200` + Liste      | —                       |
| `GET /api/books/{isbn}`    | Ein Buch lesen    | `200` + Buch       | `404` unbekannte ISBN   |
| `POST /api/books`          | Buch anlegen      | `201` + `Location` | `400` ungültige Eingabe |
| `DELETE /api/books/{isbn}` | Buch löschen      | `204` ohne Body    | `404` unbekannte ISBN   |

* Die Sammlung (`/api/books`) und das Element (`/api/books/{isbn}`) sind zwei
  Ressourcen.
* Dieser Vertrag ist der rote Faden des Moduls — jede Folie baut ein Stück
  davon.

---

# Der Controller

---

<!-- _class: densest -->
## @RestController

Der Einstieg in die Web-Schicht ist eine Bean mit zwei Annotationen:

```java

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
}
```

* `@RestController` = `@Controller` + `@ResponseBody`: Rückgabewerte landen
  direkt im Response-Body (als JSON)
* `@RequestMapping` an der Klasse wird zu einem gemeinsamen URL-Präfix für alle
  Methoden.
* Der Controller bekommt den `BookService` per Konstruktor-Injection

**Merksatz:** Der Controller übersetzt zwischen HTTP und Fachlogik. Die
eigentliche Arbeit macht der Service.

---

<!-- _class: denser -->
## @GetMapping und Verwandte

Eine Methode pro Verb-und-Pfad-Kombination:

```java

@GetMapping
public List<BookResponse> list() {
    return bookService.findAll().stream()
            .map(BookResponse::from)
            .toList();
}
```

| Annotation                      | HTTP-Verb       | In unserer Demo            |
|---------------------------------|-----------------|----------------------------|
| `@GetMapping`                   | `GET`           | `list()`, `get(...)`       |
| `@PostMapping`                  | `POST`          | `create(...)`              |
| `@DeleteMapping`                | `DELETE`        | `delete(...)`              |
| `@PutMapping` / `@PatchMapping` | `PUT` / `PATCH` | — (Bonusaufgabe der Übung) |

---

<!-- _class: denser -->
## @PathVariable

Teile der URL als Methodenparameter für den Zugriff auf **ein** Element:

```java

@GetMapping("/{isbn}")
public BookResponse get(@PathVariable String isbn) {
    return BookResponse.from(bookService.findByIsbn(isbn));
}
```

* `{isbn}` im Pfad-Template wird an den gleichnamigen Parameter gebunden.
* Die Konvertierung übernimmt Spring. Auch `Long`, `UUID` oder `LocalDate` sind
  möglich.
* Damit die Zuordnung **über den Namen** funktioniert, muss der Parametername im
  Bytecode stehen: unser POM setzt dafür das Compiler-Flag `-parameters`.
  Alternativ explizit: `@PathVariable("isbn")`.

`curl localhost:8080/api/books/978-3-8362-9049-8` → `200` mit einem Buch.

---

<!-- _class: denser -->
## @RequestParam

Query-Parameter (`?key=value`) als Methodenparameter. Genutzt z.B. für Filter,
Suche, Paging:

```java

@GetMapping("/search")
public List<BookResponse> search(@RequestParam String title) { ...}
```

* Aufruf: `GET /api/books/search?title=Java`
* `@RequestParam(required = false)` macht den Parameter optional,
  `defaultValue = "..."` liefert einen Standardwert.

Abgrenzung:

|           | Gehört in den Pfad          | Gehört in den Query-String |
|-----------|-----------------------------|----------------------------|
| Beispiel  | `/api/books/{isbn}`         | `?title=Java&page=2`       |
| Bedeutung | **Identität** der Ressource | **Variante** der Anfrage   |

---

## @RequestBody

Der Request-Body als Java-Objekt für `POST` und `PUT`:

```java

@PostMapping
public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
    Book book = bookService.create(request.toBook());
    return ResponseEntity
            .created(URI.create("/api/books/" + book.isbn()))
            .body(BookResponse.from(book));
}
```

* Jackson deserialisiert das JSON aus dem Body in einen `BookRequest`
* Der Client muss `Content-Type: application/json` senden.
* Was `@Valid` hier genau tut, klären wir im Validierungs-Teil

---

## ResponseEntity

Bisher haben unsere Methoden nur den **Body** bestimmt. `ResponseEntity` gibt
uns die volle Kontrolle über die Response: **Status, Header und Body**.

```java
return ResponseEntity
        .created(URI.create("/api/books/"+book.isbn()))  // 201 + Location-Header
        .

body(BookResponse.from(book));
```

* `.created(uri)` setzt Status `201` und den `Location`-Header. Das ist die URL,
  unter der die neue Ressource ab jetzt erreichbar ist.
* Weitere Factory-Methoden: `ResponseEntity.ok(body)`, `.notFound().build()`,
  `.noContent().build()`.

---

<!-- _class: denser -->
## Welchen Statuscode wann?

| Code              | Bedeutung                                | In der Demo                               |
|-------------------|------------------------------------------|-------------------------------------------|
| `200 OK`          | Gelesen, Antwort im Body                 | `GET /api/books`, `GET /api/books/{isbn}` |
| `201 Created`     | Ressource angelegt, `Location` zeigt hin | `POST /api/books`                         |
| `204 No Content`  | Erledigt, nichts zu berichten            | `DELETE /api/books/{isbn}`                |
| `400 Bad Request` | Client-Fehler: Eingabe ungültig          | `POST` mit leerem Titel                   |
| `404 Not Found`   | Ressource existiert nicht                | `GET`/`DELETE` mit unbekannter ISBN       |

Merkhilfe: `2xx` Erfolg, `4xx` Schuld des Clients, `5xx` Schuld des Servers.

---

# JSON

---

<!-- _class: densest -->
## Jackson: Serialisierung ohne Zutun

Woher kommt eigentlich das JSON? `spring-boot-starter-web` bringt **Jackson**
mit, die AutoConfiguration registriert es als Message-Converter.

```java

@GetMapping
public List<BookResponse> list() { ...}
```

wird zu:

```json
[
  {
    "isbn": "978-3-8362-9049-8",
    "title": "Spring Boot 3 und Spring Framework 6",
    "netPrice": 49.90,
    "grossPrice": 59.38
  }
]
```

* Jeder Record-Komponente entspricht ein JSON-Feld
* Auch beim **Deserialisieren** in `@RequestBody` wird Jackson genutzt
* Ein eigener `ObjectMapper` als Bean überschreibt die Defaults

---

<!-- _class: dense -->
## Records als DTOs

Ein **DTO** (Data Transfer Object) ist eine Klasse, deren einziger Zweck der
Datentransport über Schnittstellen ist. Dafür sind Java-Records gedacht:

```java
public record BookRequest(
        String isbn,
        String title,
        BigDecimal netPrice) {

    public Book toBook() {
        return new Book(isbn, title, netPrice);
    }
}
```

* Unveränderlich und `equals`/`hashCode`/`toString` automatisch erzeugt
* Unsere Demo trennt Richtungen: `BookRequest` für Eingaben, `BookResponse` für
  Ausgaben

---

## Warum DTOs statt Entities?

Warum nicht einfach `Book` direkt zurückgeben?

* **Die Schnittstelle wird sonst zum Abbild des Innenlebens.** Jede Umbenennung
  im Domänenmodell ändert ungewollt das JSON. Das bricht Clients, von denen wir
  nichts wissen.
* **Die Schnittstelle braucht andere Felder.** `BookResponse` liefert z.B. den
  Bruttopreis, den `Book` nicht kennt. Umgekehrt gehören interne Felder nicht
  nach draußen.

---

<!-- _class: dense -->
## Felder umbenennen und ausblenden

Wenn JSON-Name und Java-Name auseinanderlaufen sollten, steuern
Jackson-Annotationen die Abbildung:

```java
// Beispiele — unsere Demo braucht sie nicht:
record BookResponse(
                @JsonProperty("ean") String isbn,   // heißt im JSON "ean"
                String title,
                @JsonIgnore BigDecimal netPrice,    // fehlt im JSON komplett
                BigDecimal grossPrice) {
}
```

* `@JsonProperty("...")`: anderer Feldname im JSON als im Code.
* `@JsonIgnore`: Feld wird weder serialisiert noch deserialisiert.
* `@JsonInclude(NON_NULL)`: `null`-Felder weglassen statt `"field": null` zu
  senden.

---

# Validierung

---

<!-- _class: dense -->
## Bean Validation einbinden

Eingaben von Clients sind **immer** unzuverlässig. Validierung ist daher
Pflicht. Der Standard dafür: **Bean Validation** (Jakarta Validation), in Boot
mit einem eigenen Starter:

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

* Bringt den **Hibernate Validator** mit: die Referenzimplementierung (hat
  nichts mit Hibernate ORM zu tun).
* Ohne diesen Starter sind `@NotBlank` und Co. wirkungslos: Die Annotationen
  kompilieren, prüfen aber nichts.
* **deklarativ** beschreiben, was gültig ist, statt `if`-Kaskaden im Controller.

---

<!-- _class: denser -->
## Die wichtigsten Constraints

Die Annotationen stehen direkt am DTO. Unser `BookRequest`:

```java
public record BookRequest(
        @NotBlank String isbn,
        @NotBlank @Size(max = 200) String title,
        @NotNull @Positive BigDecimal netPrice) {
}
```

| Constraint        | Prüft                                          | Passend für                     |
|-------------------|------------------------------------------------|---------------------------------|
| `@NotBlank`       | Nicht `null`, nicht leer, nicht nur Whitespace | Pflicht-Strings                 |
| `@NotNull`        | Nicht `null`                                   | Alle Nicht-String-Pflichtfelder |
| `@Size(min, max)` | Länge von Strings und Collections              | Titel, Listen                   |
| `@Positive`       | Zahl größer null                               | Preise, Mengen                  |

---

## @Valid am Controller

Die Constraints allein prüfen noch nichts. Erst `@Valid` am Parameter löst die
Prüfung aus:

```java

@PostMapping
public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
    ...
}
```

* Reihenfolge im Ablauf: Jackson deserialisiert den Body → Bean Validation prüft
  das Objekt → erst dann startet die Methode.
* Schlägt die Prüfung fehl, wird die Controller-Methode nie betreten. Ungültige
  Daten erreichen die Fachlogik nicht.

---

<!-- _class: densest -->
## Was passiert bei einem Verstoß?

Ein `POST` mit leerem Titel gegen unsere Demo:

```bash
curl -i -X POST localhost:8080/api/books \
     -H "Content-Type: application/json" \
     -d '{"isbn":"978-0-13-468599-1","title":"","netPrice":49.99}'
```

* Spring wirft eine `MethodArgumentNotValidException` und beantwortet sie von
  sich aus mit `400 Bad Request`.
* Der Standard-Body ist allerdings karg: Der Client erfährt nicht, welches Feld
  falsch war.
* Unsere Demo ergänzt deshalb einen Handler, der die Feldfehler als Map
  `Feldname → Meldung` zurückgibt:

```json
{
  "title": "must not be blank"
}
```

Wie dieser Handler aussieht sehen wir im nächsten Abschnitt

---

# Fehlerbehandlung

---

<!-- _class: densest -->
## @ExceptionHandler im Controller

Erst das Problem: `GET /api/books/999`: der `BookService` wirft eine
`BookNotFoundException`, die nicht gefangen wird. Ergebnis: **
`500 Internal Server Error`**. Fachlich ist das falsch: der Client hat die
falsche ISBN geschickt, nicht der Server versagt.

Erster Lösungsansatz: ein Handler **im Controller**:

```java
// Variante 1: lokal im BookController — funktioniert, skaliert aber nicht
@ExceptionHandler(BookNotFoundException.class)
@ResponseStatus(HttpStatus.NOT_FOUND)
public Map<String, String> handleBookNotFound(BookNotFoundException exception) {
    return Map.of("error", exception.getMessage());
}
```

* `@ExceptionHandler` markiert eine Methode als Auffangstelle für einen
  Exception-Typ.
* `@ResponseStatus` bestimmt den Statuscode, der Rückgabewert wird der
  JSON-Body.
* Haken: Der Handler gilt **nur für diesen einen Controller**.

---

<!-- _class: densest -->
## @RestControllerAdvice global

Dieselbe Methode kann man auslagern, sodass sie für alle Klassen gilt:

```java

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleBookNotFound(BookNotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }
}
```

* `@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`: Rückgabewerte
  der Handler landen direkt im Response-Body, dasselbe Muster wie bei
  `@RestController`.

Warum global besser ist:

* **Ein** Ort für die Abbildung Fachfehler, keine Duplizierung
* Controller bleiben frei von `try`/`catch`
* Konsistente Fehlerantworten über die ganze API

---

<!-- _class: dense -->
## Eine eigene Fehlerantwort bauen

Der zweite Handler unserer Demo macht aus einem einfach `400` eine brauchbare
Antwort:

```java

@ExceptionHandler(MethodArgumentNotValidException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public Map<String, String> handleValidation(MethodArgumentNotValidException exception) {
    Map<String, String> errors = new LinkedHashMap<>();
    exception.getBindingResult().getFieldErrors().forEach(fieldError ->
            errors.put(fieldError.getField(), fieldError.getDefaultMessage()));
    return errors;
}
```

* Die `MethodArgumentNotValidException` trägt alle Feldfehler im `BindingResult`
* Beachten: Die Exception **stammt von Spring**, nicht von uns. Auch
  Framework-Exceptions lassen sich per Advice übersetzen.

---

# HTTP aufrufen

---

## RestClient

Bisher haben wir Server-Code geschrieben. Oft ist unsere Anwendung aber auch
**Client** einer anderen API. Der aktuelle synchrone HTTP-Client in Spring:
`RestClient`.

```java
RestClient client = RestClient.create("http://localhost:8080");
```

* **Fluent API** und synchrone Calls. Seit Spring Framework 6.1 vorhanden und
  der Standard in Spring Boot 4.
* Nutzt dieselbe Jackson-Bibliothek wie die Server-Seite
* Das ältere `RestTemplate` erfüllt denselben Zweck, hat aber eine angestaubte
  API

---

<!-- _class: denser -->
## GET und POST mit RestClient

```java
// GET /api/books/{isbn} → BookResponse
BookResponse book = client.get()
                .uri("/api/books/{isbn}", "978-3-8362-9049-8")
                .retrieve()
                .body(BookResponse.class);

// POST /api/books mit JSON-Body
BookResponse created = client.post()
        .uri("/api/books")
        .contentType(MediaType.APPLICATION_JSON)
        .body(new BookRequest("978-0-13-468599-1", "Effective Java",
                new BigDecimal("49.99")))
        .retrieve()
        .body(BookResponse.class);
```

* `retrieve()` führt den Aufruf aus; `body(Class)` deserialisiert die Antwort.
* Bei `4xx`/`5xx` wirft `retrieve()` eine Exception. Auch die Client-Seite kommt
  also nicht ohne Fehlerbehandlung aus.

---

# Demo

---

<!-- _class: denser -->
## Was wir gleich bauen

`demos/sb-basics-web-demo` ist der Startpunkt. Die Fachlogik ist fertig (`Book`,
`BookService`, `BookNotFoundException`) aber die Web-Schicht **leer**. Wir bauen
sie live:

| Schritt | Baustein                           | Ziel                                       |
|---------|------------------------------------|--------------------------------------------|
| 1       | `BookController` mit `@GetMapping` | JSON ohne Konfiguration                    |
| 2       | `BookResponse` mit `grossPrice`    | Schnittstelle vom Domänenmodell entkoppelt |
| 3       | `@PathVariable` für `GET /{isbn}`  | Unbekannte ISBN → **`500`** statt `404`    |
| 4       | `RestExceptionHandler`             | Derselbe `curl` liefert jetzt `404`        |
| 5       | `BookRequest` + `@Valid` + `POST`  | `201` mit `Location`; leerer Titel → `400` |

---

# Übung

---

<!-- _class: densest -->
## Assignment 02

`assignments/sb-basics-web-assignment` ist eine Übung mit einer neuen Fachlichkeit zur Abwechslung: eine **Kursverwaltung**

* Gegeben: `CourseAdminApplication`, `Course`, `CourseService` (wirft
  `CourseNotFoundException`).
* Zu bauen: `CourseRequest`, `CourseResponse`, `CourseController`,
  `RestExceptionHandler`.

Vier Aufgaben, wobei jede durch einen Test abgesichert ist:

1. `GET /api/courses`: Liste mit berechnetem `grossFee`
2. `GET /api/courses/{code}`: `404` bei unbekanntem Kurscode
3. `POST /api/courses`: `201` mit `Location`-Header
4. Validierung des `CourseRequest`: `400` per `@RestControllerAdvice`

* Bonus: `PUT /api/courses/{code}` mit `200`/`404`.
* Musterlösung: `solutions/sb-basics-web-solution`.

**Ausblick:** Modul 03 gibt der Buchhandlung eine echte Datenbank
