# Testing mit Spring Boot

## Basics

Als Erstes benötigen wir die Test-Dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

Zeige im Dependency Tree, dass jetzt schon JUnit Jupiter enthalten ist.

Erstelle eine Testklasse mit `@SpringBootTest`. Demonstriere, dass sich Services autowiren und Properties injecten lassen.

```java
@SpringBootTest
public class ApplicationContextTest {

    @Autowired
    HelloService helloService;

    @Value("${tech.erben.springboot.testing.helloService.message}")
    String message;

    @Test
    public void basicTest() {

        System.out.println(helloService.sayHello());
        System.out.println(message);
    }
}
```

Auch der ApplicationContext lässt sich injecten.

```java
@Autowired
ApplicationContext context;
//...
@Test
public void basicTest() {
    System.out.println(helloService.sayHello());
    System.out.println(message);
    System.out.println(context.getId());
}
```

Erkläre, dass `SpringBootTest` automatisch einen Component Scan auf dem aktuellen Package und allen darunter stehenden macht - sowohl im Test- als auch im `tech.erben`-Ordner. Wir können die Klasse aber auch explizit angeben:

```java
@SpringBootTest(classes = BasicSpringApplication.class)
```

## Mocks

Gebe noch mal eine kurze Einführung zu Mocks. Danach füge folgende Dependency ein:

```java
@Autowired
MockMeBean mockMeBean;
```

Führe den Test aus und zeige in der Kommandzeile, dass wie erwartet der Konstruktur aufgerufen wird. Ersetze danach `@Autowired` durch `@MockBean` und mocke die Methode im Test:

```java
@Test
public void mockingTest() {
    Mockito.when(mockMeBean.mockMe()).thenReturn("mocked");
    System.out.println(mockMeBean.mockMe());
}
```

Zeige, dass weder Konstruktur noch richtige Methode aufgerufen wird.
Erläutere, dass `@MockBean` anders als `@Mock` auch die Bean zum Context hinzufügt.
Erstelle dazu in Mein eine Klasse wie folgt:

```java

@Service
public class InjectMockExampleService {

    @Autowired
    MockMeBean mockMeBean;

    public String callMockMeBean() {
        return mockMeBean.mockMe();
    }

}
```

Ändere im Test die Annotation für `MockMeBean` auf `@Mock` und erkläre, dass dies jetzt dazu führt, dass die Bean _nicht_ mehr in Kontext ist. Erstelle einen Test:

```java
@Test
public void mockingDependencyTest() {
    Mockito.when(mockMeBean.mockMe()).thenReturn("mocked");
    System.out.println(injectMockExampleService.callMockMeBean());
}
```

Zeige, dass er fehlschlägt. Ersetze `@Mock` wieder durch `@MockBean` und zeige, dass der Test grün wird.

## Expectations und Verifications

Bei der Gelegenheit geben wir noch mal einen Überblick über Matcher und Expectations.

```java

    @Test
    void matchersAndExpectations() {
        when(mockMeBean.mockMe()).thenReturn("test");
        System.out.println(mockMeBean.mockMe());
        
        when(mockMeBean.mockMeWithArguments(anyString())).thenReturn("withArg");
        System.out.println(mockMeBean.mockMeWithArguments(""));
        
        when(mockMeBean.mockMeWithArguments(eq("1"))).thenReturn("one");
        when(mockMeBean.mockMeWithArguments(eq("2"))).thenReturn("two");
        System.out.println(mockMeBean.mockMeWithArguments("1"));
        System.out.println(mockMeBean.mockMeWithArguments("2"));

        Mockito.reset(mockMeBean);
        mockMeBean.mockMe();
        verify(mockMeBean).mockMe();

        Mockito.reset(mockMeBean);
        mockMeBean.mockMeWithArguments("test");
        verify(mockMeBean).mockMeWithArguments("test");

        Mockito.reset(mockMeBean);
        mockMeBean.mockMe();
        mockMeBean.mockMe();
        verify(mockMeBean, times(2)).mockMe();

        Mockito.reset(mockMeBean);
        verifyNoInteractions(mockMeBean);

    }
```

## Exceptions

Hier ist ein Snippet, um Exception Mocking und Assertions zu demonstrieren.

```java
@Test
void mockAndTestExceptions() {
    when(mockMeBean.mockMeWithArguments(null))
        .thenThrow(new IllegalArgumentException());
    assertThrows(IllegalArgumentException.class, () -> mockMeBean.mockMeWithArguments(null));
}
```

## Web Tests

Gehe Schritt für Schritt den folgenden Testfall durch.
Erläutere dabei:

- WebMvcTest: Es ist kein "richtiger" Kontext. Zeige im Log `o.s.t.web.servlet.TestDispatcherServlet`. Kann aber ansonsten das Gleiche wie `@SpringBootTest`
- MockMvc Expectations
- MockMvc Multipart
- MockMvc Post Body

```java

@WebMvcTest(FileController.class)
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testHttpGetWithJsonExpectations() throws Exception {
        String fileId = "1";
        mockMvc.perform(get("/files?id=" + fileId)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(fileId))
            .andExpect(jsonPath("$.name").value("example.txt"));
    }

    @Test
    public void testHttpFileUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt",
            MediaType.TEXT_PLAIN_VALUE, "test_file_upload".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/files/upload").file(file))
            .andExpect(status().isOk())
            .andExpect(content().string("File uploaded successfully."));
    }

    @Test
    public void testHttpPostWithJson() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("key", "value");

        mockMvc.perform(post("/files/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.key").value("value"));
    }
}
```

Zeige nun eine Alternative, die einen echten ApplicationContext hochfährt:

```java

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestRestTemplateFileControllerTest {

    @MockBean
    MockMeBean mmb;
    
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testHelloIntegration() {
        ResponseEntity<String> response = restTemplate.getForEntity("/files?id=test", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(response);
    }
}
```

Hier müssen wir `MockMeBean` mocken, weil ansonsten der Context nicht hochfährt. Das war beim MockMvcTest nicht nötig.

## Datenbank-Testing

Füge diese Deps hinzu:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

Implementiere und erläutere diesen Test:

```java
@DataJpaTest
@EntityScan("tech.erben.springboot.testing")
public class UserServiceTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UserRepository userRepository;
    
    @Test
    public void testCreateUser() {
        UserEntity newUser = new UserEntity();
        newUser.setName("John Doe");
        newUser.setEmail("john.doe@example.com");

        testEntityManager.persist(newUser);
        testEntityManager.flush();

        List<UserEntity> users = userRepository.findAll();
        assertThat(users.size()).isEqualTo(1);
        assertThat(users.get(0).getName()).isEqualTo(newUser.getName());
        assertThat(users.get(0).getEmail()).isEqualTo(newUser.getEmail());
    }
}

```
