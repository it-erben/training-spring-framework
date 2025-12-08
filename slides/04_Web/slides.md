---
marp: true
theme: default
header: Spring Boot Advanced
footer: Alexander Erben
paginate: true
---

# RESTful Web Services in Spring Boot

---

## In diesem Modul
*   REST-Controller Basics: ResponseEntity, Content Negotiation
*   Validation & Error Handling
*   Moderne HTTP Clients: RestClient, Declarative HTTP Interfaces
*   Async/Streaming: CompletableFuture, Server-Sent Events

---

## Wiederholung: Der @RestController
*   Spezielle `Controller`-Annotation, die `Controller` und `@ResponseBody` kombiniert.
*   Jede Methode gibt direkt Daten zurück (keine View-Auflösung).
*   Behandelt JSON/XML-Serialisierung automatisch.

---

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        // ... Logik
        return new User(id, "Alice");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Setzt den HTTP Status 201
    public User createUser(@RequestBody User user) {
        // ... Logik
        return user;
    }
}
```

---

## Wiederholung: ResponseEntity
Volle Kontrolle über HTTP Response (Status, Header, Body).

```java
@GetMapping("/{id}")
public ResponseEntity<User> findUser(@PathVariable Long id) {
    Optional<User> user = userService.findById(id);
    return user.map(ResponseEntity::ok) // 200 OK
               .orElse(ResponseEntity.notFound().build()); // 404 Not Found
}
```

---

## DTO-Validierung

Das Bean Validation-Framework wird auch vom Spring Framework unterstützt, um Daten mit verschiedenen Annotationen zu prüfen.
Die wichtigsten Annotationen:

*   `@Valid`: Standard-JSR 380 (Bean Validation) Annotation.
*   `@Validated`: Spring-spezifisch, unterstützt Validation Groups.

---

## DTO-Validierung

```java
// DTO für die Anfrage
public class UserCreateDto {
    @NotBlank(message = "Name darf nicht leer sein")
    @Size(min = 3, max = 50, message = "Name muss 3-50 Zeichen haben")
    private String name;

    @Email(message = "Ungültiges E-Mail Format")
    private String email;
    
    // Getter & Setter
} 

@PostMapping
public ResponseEntity<User> createUser(@Valid @RequestBody UserCreateDto userDto) {
    // Wenn Validation fehlschlägt, wird eine MethodArgumentNotValidException geworfen
    // ...
}
```
---

## Validation Groups
Unterschiedliche Regeln für verschiedene Szenarien (z.B. Erstellen vs. Aktualisieren).

```java
// Interfaces als Marker
public interface OnCreate {}
public interface OnUpdate {}

public class UserDto {
    @NotNull(groups = OnUpdate.class) // Nur bei Update nötig
    private Long id;
    
    @NotBlank(groups = OnCreate.class) // Nur bei Create nötig
    private String name;
}

@PostMapping
public ResponseEntity<User> create(@Validated(OnCreate.class) @RequestBody UserDto dto) { ... }

@PutMapping
public ResponseEntity<User> update(@Validated(OnUpdate.class) @RequestBody UserDto dto) { ... }
```

---

## Custom Validators
Eigene Validierungslogik implementieren.

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "E-Mail bereits vergeben";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    @Autowired private UserRepository userRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        return userRepository.findByEmail(email).isEmpty();
    }
}
```

---

## Globales Error Handling
Zentrales Fehlerhandling für die gesamte REST-API.

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validierungsfehler");
        pd.setTitle("Invalid Request Body");
        return pd;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
    }
}
```
---
## ProblemDetails (RFC 7807)
Standardisiertes Format für HTTP API Fehlerantworten (seit Spring Boot 3).

```json
{
  "type": "about:blank",
  "title": "Invalid Request Body",
  "status": 400,
  "detail": "Validierungsfehler",
  "instance": "/api/v1/users",
  "errors": {
    "name": "Name darf nicht leer sein",
    "email": "Ungültiges E-Mail Format"
  }
}
```
Spring Boot konvertiert `ProblemDetail` automatisch in JSON oder XML, wenn der `Accept`-Header dies verlangt.

---

## ProblemDetails erweitern

Oft reicht der Standard nicht. Wir wollen z.B. eine `traceId` oder spezifische Business-Error-Codes hinzufügen.

```java
@ExceptionHandler(MyBusinessException.class)
public ProblemDetail handleBusinessException(MyBusinessException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, ex.getMessage());
    
    // Eigene Properties hinzufügen
    pd.setProperty("errorCode", "BUS-001");
    // Angenommen wir haben einen Tracer injectet
    pd.setProperty("traceId", tracer.currentSpan().context().traceId());
    
    return pd;
}
```

---

# Moderne HTTP Clients

---

## Status Quo: RestTemplate
*   Lange Zeit der Standard für synchrone Calls.
*   Jetzt im **Maintenance Mode**. Es wird keine neuen Features mehr geben.
*   Nachteil: Viele überladene Methoden, kein Fluent API.

**Die Nachfolger:**
1.  **RestClient (Spring Boot 3.2):** Synchron, Fluent API. Basiert auf Servlet-Stack.
2.  **WebClient (Spring 5):** Reaktiv, non-blocking. Erfordert `spring-boot-starter-webflux`.
3.  **Declarative HTTP Interfaces (Spring 6):** Interface-basiert (via Proxy).

---

## Der RestClient (Synchron)
Bietet eine moderne Fluent API ohne Reactive Stack (Mono/Flux).

```java
@Service
public class ProductClient {
    private final RestClient restClient;

    public ProductClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://api.example.com").build();
    }

    public Product getProduct(String id) {
        return restClient.get()
            .uri("/products/{id}", id)
            .retrieve()
            .body(Product.class);
    }
}
```

---

## Declarative HTTP Interfaces
Definiere die API als Java Interface (ähnlich Feign/Retrofit).

```java
public interface UserApi {
    @GetExchange("/users/{id}")
    User getById(@PathVariable Long id);

    @PostExchange("/users")
    void createUser(@RequestBody User user);
}
```
Dies benötigt einen **Unterbau**, der die Requests ausführt (WebClient oder RestClient).

---

## Declarative Client Factory (mit RestClient)
Verbindung von Interface und Engine.

```java
@Configuration
public class ClientConfig {
    @Bean
    UserApi userApi(RestClient.Builder builder) {
        RestClient client = builder.baseUrl("https://user-service").build();
        
        // Nutzt den synchronen RestClient als Engine
        RestClientAdapter adapter = RestClientAdapter.create(client);
        
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(adapter)
            .build();
            
        return factory.createClient(UserApi.class);
    }
}
```

---

# Asynchrone APIs & Streaming

---

## `CompletableFuture` als Rückgabetyp
*   Der Controller Thread wird freigegeben, während die Logik im Hintergrund arbeitet.
*   Verbessert die Skalierbarkeit bei blockierenden Operationen.


```java
@RestController
public class AsyncController {
    @Autowired private SlowService slowService;

    @GetMapping("/async-result")
    public CompletableFuture<String> getAsyncResult() {
        return CompletableFuture.supplyAsync(() -> slowService.doSlowWork());
    }
}
```

---

## Streaming Responses (Server-Sent Events)
Für Realtime-Updates, z.B. wenn der Client ständig neue Daten erhalten soll.

```java
@RestController
public class SseController {

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        SseEmitter emitter = new SseEmitter();
        
        new Thread(() -> {
            // Hier: Events asynchron an den Emitter senden
            // z.B. aus einem Message Queue Listener oder einem Scheduled Task
            emitter.send(SseEmitter.event().name("message").data("Hello, Client!"));
            emitter.complete(); // Verbindung schließen
        }).start();

        return emitter;
    }
}
```

---

# OpenAPI (früher Swagger)

---
## Ziel
Standardisierte, maschinenlesbare Beschreibung von REST-APIs.
*   **Dokumentation:** Interaktive UI (Swagger UI).
*   **Code-Generierung:** Clients in jeder Sprache.

Man unterscheidet zwei Ansätze: Code First und Contract First.

---
### Ansatz 1: Code-First (SpringDoc OpenAPI)
Man schreibt den Code, die Doku wird daraus generiert.

```java
@RestController
@RequestMapping("/products")
@Tag(name = "Produktverwaltung", description = "API für CRUD-Operationen an Produkten")
public class ProductController {

    @Operation(
        summary = "Produkt anhand ID abrufen",
        description = "Gibt ein Produktobjekt basierend auf der bereitgestellten ID zurück.",
        parameters = @Parameter(name = "id", description = "ID des Produkts", example = "123")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produkt gefunden",
                     content = @Content(schema = @Schema(implementation = Product.class))),
        @ApiResponse(responseCode = "404", description = "Produkt nicht gefunden")
    })
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return new Product(id, "Advanced Widget");
    }
}
```

---


### Ansatz 2: Contract-First
Man schreibt zuerst die OpenAPI-Spezifikation (YAML/JSON) und generiert daraus den Code (Interfaces, DTOs).

**Vorteile:**
*   **API-Design als erste Klasse:** Fokus auf das API-Design, bevor implementiert wird.
*   **Parallele Entwicklung:** Backend- und Frontend-Teams können gleichzeitig arbeiten.
*   **Konsistenz:** API ist über alle Services hinweg konsistent.

**Tool:** `openapi-generator-maven-plugin` (oder Gradle Plugin).
