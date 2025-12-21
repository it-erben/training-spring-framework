# Web & HTTP Demo

Kurzziel: Während der Live-Demo schrittweise vom leeren Startprojekt zur fertigen API (JSON/XML, Validierung, ProblemDetails, Versionierung/HATEOAS, SSE, RestClient & deklarative HTTP-Interfaces).

Startpunkt: `code/sb-advanced-web/sb-advanced-web-demo-start`  
Fertige Lösung zum Nachsehen: `code/sb-advanced-web/sb-advanced-web-demo-finished`

## 0. Projekt starten

1) Terminal in `code/sb-advanced-web` öffnen.  
2) Build/Run (Entwickel-Loop):  
   - `mvn -f ../pom.xml -pl sb-advanced-web/sb-advanced-web-demo-start -am spring-boot:run`  
   - Alternative ohne Watch: `mvn -f ../pom.xml -pl sb-advanced-web/sb-advanced-web-demo-start -am compile`

## 1. Domain & DTOs anlegen

1) Klasse `model/Book` (POJO mit id, title, author, category, price, publicationYear, originCountry, createdAt).  
2) DTO `api/BookRequest` mit Bean Validation: `@NotBlank/@Size` (title/author), `@Positive` (price), `@Min/@Max` (Jahr), ISO Country via `@Pattern`.  
3) Validation-Gruppen: Interfaces `validation/OnCreate`, `validation/OnUpdate`.  
4) Custom Constraint: Annotation `@AllowedCategory` + Validator mit Whitelist (technology, fiction, science, business, children).  
5) Response-DTO `api/BookResponse` als `record`, mit `@JacksonXmlRootElement` für XML.

## 2. Service-Schicht

1) `service/BookService`: `ConcurrentHashMap` als Katalog, `AtomicLong` als Sequence, Seed-Daten im Konstruktor.  
2) Methoden: `findAll`, `findById` (wirft `BookNotFoundException`), `create`, `update` (partiell), `delete`.  
3) `BookNotFoundException` als RuntimeException.

## 3. Controller V1 (JSON/XML, Async, Validation)

1) `web/BookControllerV1` mit `@RestController`, `@RequestMapping(value="/api/v1/books", produces={application/json, application/xml})`.  
2) Endpunkte:  
   - `GET /books` → Liste  
   - `GET /books/{id}` → Einzelnes Book  
   - `GET /books/{id}/async` → `CompletableFuture` Demo  
   - `POST /books` (consumes JSON, `@Validated(OnCreate.class)`) → `201` + `Location`  
   - `PUT /books/{id}` (`@Validated(OnUpdate.class)`)  
   - `DELETE /books/{id}` → `204`  
3) DTO-Mapping auf `BookResponse`.
4) Content Negotiation kurz zeigen:  
   - JSON: `curl localhost:8080/api/v1/books/1`  
   - XML: `curl -H "Accept: application/xml" localhost:8080/api/v1/books/1`

## 4. Fehlerbehandlung via ProblemDetails

1) Property setzen: `spring.mvc.problemdetails.enabled=true`.  
2) `web/ApiErrorHandler` (`@RestControllerAdvice`):  
   - `BookNotFoundException` → 404 mit Titel.  
   - `MethodArgumentNotValidException`/`ConstraintViolationException` → 400 + `errors`-Map.  
   - Fallback `Exception` → 500.  
3) Demo: invalides POST → `application/problem+json` mit Fehlerdetails.

## 5. Versionierung & HATEOAS (V2)

1) `web/BookControllerV2` unter `/api/v2/books`, liefert `CollectionModel<EntityModel<BookResponse>>`.  
2) Links setzen: `self`, `collection`, Rel-Link auf V1 (`/api/v1/books/{id}`) für Pfad-Versionierung.  
3) Content Negotiation erneut mit JSON/XML zeigen.

## 6. Streaming (SSE)

1) `web/NotificationController` mit `SseEmitter`, Endpoint `/api/notifications/stream` (`produces = text/event-stream`).  
2) Demo: `curl -N localhost:8080/api/notifications/stream` → Heartbeats.

## 7. RestClient & deklarative HTTP Interfaces

1) `client/RestClientConfig`: `RestClient` mit `demo.remote.base-url` (Default `http://localhost:8080`), `BookHttpApi` via `HttpServiceProxyFactory`, sowie `BookClient`.  
2) `client/BookHttpApi`: `@HttpExchange("/api/v1/books")`, `@GetExchange`, `@PostExchange`.  
3) `client/BookClient.logSampleCalls()` aufrufen über `ApplicationRunner`, aber nur wenn `demo.restclient.log-sample=true` gesetzt ist (sonst keine Outbound-Calls).  
4) Demo optional: Property setzen, App neu starten, Log-Ausgabe zeigt RestClient + Interface Call.

## 8. Endpunkte zum Zeigen (Shortlist)

- `GET /api/v1/books`  
- `GET /api/v1/books/1` (JSON/XML)  
- `POST /api/v1/books` mit Body (Validierung + Location)  
- `GET /api/v2/books` (HATEOAS Links)  
- `GET /api/notifications/stream` (SSE)  
- Optional: RestClient/HTTP-Interface Logs mit `demo.restclient.log-sample=true`
