
### Übung 1: JUnit 5 Basics & Parametrisierung

- Schreibe parametrisierte Tests für den `StringValidator` (`@ValueSource`, `@CsvSource`, `@MethodSource`).
    > Hinweis: Die benötigten Annotationen lauten `@ParameterizedTest`, `@ValueSource`, `@CsvSource`, `@MethodSource`.
- Nutze verschachtelte Tests (`@Nested`) und ein kleines Beispiel für `@TestFactory` (dynamische Tests).
    > Die Grundstruktur eines DynamicTests sieht folgendermaßen aus:
>
    > ```java
    > @TestFactory
    > Stream<DynamicTest> dynamicTests() {
    >     return Stream.of(
    >         DynamicTest.dynamicTest("test1", () -> { /* ... */ }),
    >         DynamicTest.dynamicTest("test2", () -> { /* ... */ })
    >     );
    > }
    > ```

- Zeige bedingte Ausführung mit `@EnabledOnOs`, `@EnabledIfEnvironmentVariable` oder `@EnabledIf` an einem beliebigen Beispiel.

### Übung 2: Test Slices

- Erstelle einen Test für den `PersonController` mit `@WebMvcTest`. Mocke das `PersonService` und prüfe den GET `/api/person`.
    > Die benötigten Annotationen lauten: `@WebMvcTest(PersonController.class)` und `@MockBean`.
- `@JsonTest`: Teste die Serialisierung des `Person`-Modells.
    > Dazu benötigst du den `JacksonTester`, den du dir autowiren kannst. Er enthält Methoden wie `assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Alice");` für die Assertions.
- Rest Client Test: Erstelle einen Test, der `MockRestServiceServer` verwendet, um die Klasse `QuoteClient` zu testen. Dazu müsst du Konfigurationsvariable  `quote.api.base-url` stubben.
    > Du benötgist dafür auf der Klasse die Annotationen
`@RestClientTest(QuoteClient.class)` sowie
`@org.springframework.test.context.TestPropertySource(...)`. Die Grundstruktur für ein Matching auf dem Mock-Server ist:
>
    >```java
    >server
    >   .expect(requestTo("..."))
    >   .andRespond(
    >       withSuccess(
    >          """
    >          ...
    >          """,
    >          MediaType.APPLICATION_JSON
    >      )
    >   );
    >```

### Übung 3: Mocking & Spying

- Erstelle einen `@SpringBootTest`, der `PersonRepository` als `@MockBean` ersetzt und `PersonLoggingService` als `@SpyBean` nutzt. Verifiziere, dass `createPerson` beides aufruft.
- Nutze `CapturedOutput` bzw. `OutputCaptureExtension`, um Log-Ausgaben von `PersonLoggingService` zu prüfen.
    > Beispiel für das Setup:
>
    > ```java
    > @ExtendWith(OutputCaptureExtension.class)
    > @SpringBootTest
    > class PersonLoggingServiceTest {
    >     
    >    @Test
    >    void capturesLogOutput(CapturedOutput output) {
    > ```

### Übung 4: Datenbanktests mit Testcontainers (moderne Variante)

- Schreibe einen `@DataJpaTest` mit Postgres-Testcontainer auf moderne Weise, also mit `@ServiceConnection`.
  > Man setzt ein Postgres-Test mit Testcontainers folgendermaßen auf:
>
  > ```java
  >  @DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
  >  @Testcontainers
  >  @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
  >  class PersonRepositoryPostgresTest {
  >  
  >      @Container
  >      @ServiceConnection
  >      static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
  >          "postgres:15-alpine"
  >     );

    ```

- Optional: Vergleiche Laufzeit/Startverhalten mit/ohne Container. Dazu musst du einfach nur das Setup von Testcontainers entfernen, damit `@DataJpaTest` das Rpository mockt.

### Bonus: Eigene Extension

- Implementiere die Klasse `support/TimingExtension` (Start-Projekt) mit `BeforeTestExecutionCallback`/`AfterTestExecutionCallback`, die die Testdauer misst und ausgibt.
- Nutze die Extension in einem Beispieltest und zeige, dass Laufzeit erfasst wurde.
