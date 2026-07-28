# Demo: Web und REST — Controller, DTOs, Validierung und Fehlerbehandlung

Diese Demo baut die REST-Schnittstelle der Buchhandlung: Ein
`@RestController` liefert Buecher als JSON aus, eigene Request- und
Response-DTOs (Records) trennen die Schnittstelle vom Domaenenmodell,
Bean Validation prueft Eingaben, und ein `@RestControllerAdvice`
uebersetzt fachliche Ausnahmen in die richtigen HTTP-Statuscodes.

## REST-Vertrag

| Endpunkt | Erfolg | Fehler |
| --- | --- | --- |
| `GET /api/books` | `200`, Liste von `BookResponse` | — |
| `GET /api/books/{isbn}` | `200`, ein `BookResponse` | `404` bei unbekannter ISBN |
| `POST /api/books` | `201` mit `Location`-Header | `400` bei Validierungsfehler |
| `DELETE /api/books/{isbn}` | `204` ohne Body | `404` bei unbekannter ISBN |

Das Response-DTO enthaelt zusaetzlich zum Nettopreis den berechneten
Bruttopreis (19 % Mehrwertsteuer, kaufmaennisch gerundet):

```java
record BookRequest(String isbn, String title, BigDecimal netPrice) { }
record BookResponse(String isbn, String title, BigDecimal netPrice, BigDecimal grossPrice) { }
```

## Varianten

| Modul | Inhalt |
| --- | --- |
| `sb-basics-web-demo-start` | Fachlogik fertig (`Book`, `BookService`, `BookNotFoundException`), aber ohne Web-Schicht — Controller, DTOs und Exception-Handler entstehen live. |
| `sb-basics-web-demo-finished` | Vollstaendige REST-Schnittstelle inklusive Tests. |

## Ablauf der Live-Demo

Ausgangspunkt ist die `-start`-Variante. Jeder Zwischenzustand startet —
nach jedem Schritt lohnt ein Neustart plus `curl`, um die Wirkung zu
zeigen. Vorab einmal ohne Aenderung starten: Der Tomcat laeuft auf Port
8080, aber `curl localhost:8080/api/books` liefert `404` — es gibt noch
keinen einzigen Endpunkt.

1. **Erster Controller:** `BookController` anlegen — `@RestController`,
   `@RequestMapping("/api/books")`, Konstruktor-Injection des
   `BookService` (bekannt aus Modul 00) und eine erste Methode:

   ```java
   @GetMapping
   public List<Book> list() {
       return bookService.findAll();
   }
   ```

   `curl localhost:8080/api/books` liefert jetzt `200` mit einem
   JSON-Array — Jackson serialisiert die Records automatisch, niemand hat
   einen Konverter konfiguriert. Auffaellig: Die Antwort zeigt das nackte
   Domaenenmodell mit `netPrice`.

2. **Response-DTO:** `BookResponse`-Record mit zusaetzlichem `grossPrice`
   und statischer Factory-Methode `from(Book)` anlegen (19 %
   Mehrwertsteuer, `RoundingMode.HALF_UP`), dann `list()` auf
   `List<BookResponse>` umstellen. Der `curl` zeigt jetzt zu jedem Buch
   den Bruttopreis — und die Schnittstelle ist vom Domaenenmodell
   entkoppelt: `Book` kann sich aendern, ohne dass Clients es merken.

3. **Pfadvariable:** Einzelabruf ergaenzen:

   ```java
   @GetMapping("/{isbn}")
   public BookResponse get(@PathVariable String isbn) {
       return BookResponse.from(bookService.findByIsbn(isbn));
   }
   ```

   `curl localhost:8080/api/books/978-3-8362-9049-8` liefert `200` mit
   einem Buch. Aber `curl -i localhost:8080/api/books/999` liefert
   `500` — die `BookNotFoundException` aus dem Service kommt ungefiltert
   oben an, und eine unbehandelte `RuntimeException` ist fuer Spring ein
   Serverfehler. Fachlich falsch: Der Client hat den Fehler gemacht,
   nicht der Server.

4. **Exception-Handler:** `RestExceptionHandler` anlegen —
   `@RestControllerAdvice`, eine Methode mit
   `@ExceptionHandler(BookNotFoundException.class)` und
   `@ResponseStatus(HttpStatus.NOT_FOUND)`, die eine kleine Fehler-Map
   zurueckgibt. Derselbe `curl` liefert jetzt `404` mit
   `{"error":"Kein Buch mit ISBN 999 gefunden"}` — zentrale
   Fehlerbehandlung fuer alle Controller, kein `try/catch` im Controller.

5. **POST mit Validierung:** `BookRequest`-Record mit
   Bean-Validation-Annotationen anlegen — `@NotBlank isbn`,
   `@NotBlank @Size(max = 200) title`, `@NotNull @Positive netPrice` —
   und den Endpunkt dazu:

   ```java
   @PostMapping
   public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
       Book book = bookService.create(request.toBook());
       return ResponseEntity
               .created(URI.create("/api/books/" + book.isbn()))
               .body(BookResponse.from(book));
   }
   ```

   Ein gueltiger POST liefert `201` mit `Location`-Header auf die neue
   Ressource:

   ```bash
   curl -i -X POST localhost:8080/api/books \
        -H "Content-Type: application/json" \
        -d '{"isbn":"978-0-13-468599-1","title":"Effective Java","netPrice":49.99}'
   ```

   Mit leerem Titel oder negativem Preis kommt `400` — dank `@Valid`
   erreicht der Request die Controller-Methode gar nicht erst. Den Status
   liefert Spring von selbst; damit der Client auch sieht, *welches* Feld
   falsch war, bekommt der `RestExceptionHandler` noch einen Handler fuer
   `MethodArgumentNotValidException`, der die Feldfehler als Map
   `Feldname → Meldung` zurueckgibt.

6. **DELETE:** Letzter Endpunkt:

   ```java
   @DeleteMapping("/{isbn}")
   @ResponseStatus(HttpStatus.NO_CONTENT)
   public void delete(@PathVariable String isbn) {
       bookService.delete(isbn);
   }
   ```

   `curl -i -X DELETE localhost:8080/api/books/978-3-8362-8745-2` liefert
   `204` ohne Body; derselbe Aufruf ein zweites Mal `404` — der
   Exception-Handler aus Schritt 4 greift automatisch auch hier.

## Starten

```bash
cd sb-basics-web-demo-start
mvn spring-boot:run
```

```bash
cd sb-basics-web-demo-finished
mvn spring-boot:run
```

Alternativ als Fat-JAR: `mvn package` und dann
`java -jar target/sb-basics-web-demo-finished-1.0.0-SNAPSHOT.jar`.

## Tests

Nur die `-finished`-Variante hat Tests (in der `-start`-Variante fehlt
die Web-Schicht, die sie pruefen wuerden):

```bash
cd sb-basics-web-demo-finished
mvn test
```

Jede Verhaltensbehauptung ist durch einen Test in `BookControllerTest`
abgesichert:

| Behauptung | Test |
| --- | --- |
| `GET /api/books` liefert `200`, jedes Buch enthaelt `grossPrice` | `listReturnsBooksWithGrossPrice` |
| `GET` auf bekannte ISBN liefert `200`; `grossPrice` = Nettopreis + 19 %, kaufmaennisch gerundet | `knownIsbnReturnsBookWithGrossPrice` |
| `GET` auf unbekannte ISBN liefert `404` | `unknownIsbnReturnsNotFound` |
| `POST` mit gueltigem Body liefert `201` mit `Location`-Header | `createReturnsCreated` |
| `POST` mit leerem Titel liefert `400` | `createWithoutTitleReturnsBadRequest` |
| `POST` mit negativem Preis liefert `400`, der Body nennt das Feld | `createWithNegativePriceReturnsBadRequest` |
| `DELETE` auf bekannte ISBN liefert `204`, danach ist das Buch weg | `deleteRemovesBook` |
| `DELETE` auf unbekannte ISBN liefert `404` | `deleteUnknownIsbnReturnsNotFound` |
