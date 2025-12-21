# Autoconfiguration transcript

Erkläre, was das Ziel ist: Eine Anwendung bauen, die
einen Tomcat embedded und eine H2-DB enthält.
Wir wollen auf dem Weg erklären, wie Spring Boot
anhand von Conditions erkennt, was zu starten ist.

Setup:
Starte im Projekt "sb-advanced-autoconfiguration-demo-start"

## 1 Tomcat embedded

Wir steigen damit ein, dass wir uns einen Tomcat "as code" zusammenschrauben.
Erzeuge eine leere tech.erben.various.Main-Klasse mit tech.erben.various.Main-Methode. Füge die folgende Dependency zur POM hinzu:

```xml
<dependency>
    <groupId>org.apache.tomcat.embed</groupId>
    <artifactId>tomcat-embed-core</artifactId>
    <version>10.1.11</version>
</dependency>
```

Baue die rohe Application mit Embedded Tomcat:

```java
public class SampleApplication {
    public static void main(String[] args) throws LifecycleException, ServletException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector().setProperty("address", "0.0.0.0");
        
        tomcat.start();
        tomcat.getServer().await();
    }
}
```

Dieses Beispiel ist noch nicht startfähig!!!
Binde nun einen Context und ein Servlet ein. Erwähne das DispatcherServlet von Spring.

```java
public class SampleApplication {

    public static void main(String[] args) throws LifecycleException, ServletException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector().setProperty("address", "0.0.0.0");
        Context context = tomcat.addContext("", null);
        Tomcat.addServlet(context, "dateServlet", CurrentDateServlet.class.getName());
        context.addServletMappingDecoded("/", "dateServlet");
        tomcat.start();
        tomcat.getServer().await();
    }

    public static class CurrentDateServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.getWriter().write("Current date: " + LocalDateTime.now());
        }
    }
}
```

Der Server kann jetzt starten und gibt immer ein Date zurück

## 2 Spring Context

Als nächstes "springifizieren" wir das Projekt. Erwähne, dass in Legacy-Projekten nicht mehr zu sehen ist, was man für Barebones-Spring eigentlich braucht. Es reicht spring-context

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>6.1.2</version>
</dependency>
```

Extrahiere den Start des Tomcats in eine eigene Klasse

```java
public static class TomcatLauncher {
    public void launch() throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector().setProperty("address", "0.0.0.0");
        Context context = tomcat.addContext("", null);
        Tomcat.addServlet(context, "dateServlet", CurrentDateServlet.class.getName());
        context.addServletMappingDecoded("/", "dateServlet");
        tomcat.start();
        tomcat.getServer().await();
    }
}
```

füge eine Spring Java-Config hinzu und erwähne, dass das nur eine der vielen Optionen ist, einen Kontext zu erstellen:

```java
@Configuration
public static class AppConfig {
    @Bean
    public TomcatLauncher tomcatLauncher() {
        return new TomcatLauncher();
    }
}
```

Ergänze `@PostConstruct` zur `launch`-Methode um sie automatisch zu starten.
Starte den Kontext in `main`:

```java
public static void main(String[] args) throws LifecycleException, ServletException {
    new AnnotationConfigApplicationContext(AppConfig.class);
}
```

Nun kann man die AppConfig auch in ein anderes Projekt auslagern. Ändere die Configs folgendermaßen:

```java
@Configuration
public static class DefaultConfig {
    @Bean
    public TomcatLauncher tomcatLauncher() {
        return new TomcatLauncher();
    }
}

@Import(DefaultConfig.class)
public static class MyConfig {

}
```

`DefaultConfig` könnte nun auch in einer Library liegen. `@Import(DefaultConfig.class)` ist konzeptuell sehr ähnlich wie `@SpringBootApplicationContext`.

## 3 Conditional Configuration

Extrahiere `DefaultConfig` in eine eigene Datei mit allen Tomcat-spezifischem Code.

```java

@Configuration
public class DefaultConfig {
    @Bean
    public TomcatLauncher tomcatLauncher() {
        return new TomcatLauncher();
    }
    
    public static class TomcatLauncher {
        @PostConstruct
        public void launch() throws LifecycleException {
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(8080);
            tomcat.getConnector().setProperty("address", "0.0.0.0");
            Context context = tomcat.addContext("", null);
            Tomcat.addServlet(context, "dateServlet", CurrentDateServlet.class.getName());
            context.addServletMappingDecoded("/", "dateServlet");
            tomcat.start();
            tomcat.getServer().await();
        }
    }

    public static class CurrentDateServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.getWriter().write("Current date: " + LocalDateTime.now());
        }
    }
}

```

Füge eine `Conditional`-Annotation zur `Bean`-Methode hinzu. Implementiere sie mit einem Class-Lookup.

```java
@Configuration
public class DefaultConfig {
    @Bean
    @Conditional(TomcatOnClassPathCondition.class)
    public TomcatLauncher tomcatLauncher() {
        return new TomcatLauncher();
    }

    private static class TomcatOnClassPathCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            try {
                Class.forName("org.apache.catalina.startup.Tomcat");
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }

    public static class TomcatLauncher {
       // ...
    }

    public static class CurrentDateServlet extends HttpServlet {
       // ...
    }
}
```

Stelle die Tomcat-Dependency auf den Provided-Scope, importiere neu und starte die Anwendung. Zeige, dass Tomcat nicht mehr startet. Erläutere, dass dies genau die Logik hinter Spring Boot ist. Wir gehen nun erst mal auf Properties ein.
Denke daran, dass die Main-Klasse keinen Tomcat-Import mehr haben darf.

## Properties

Erläutere, dass Properties an verschiedenen Stellen definiert werden können. Klassiker: `application.properties`. Ein Überblick findet sich in der [Referenz-Dokumentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)

Lösche die `application.properties`. Füge `PropertySources` zur `DefaultConfig` hinzu:

```java
@PropertySources(
    {
        @PropertySource(value="classpath:application.properties"),
        @PropertySource(value="classpath:application-${spring.profiles.active}.properties")
    }
)
public class DefaultConfig{}
```

Starte die Anwendung und zeige die `FileNotFoundException`s. Füge `ignoreResourceNotFound` ein und erläutere, dass Spring Boot genau das macht. Starte neu und zeige, dass nur noch Logs kommen.

## H2

Füge Dependencies für H2 und Spring-JDBC ein

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-jdbc</artifactId>
    <version>6.1.2</version>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
</dependency>
```

Erstelle eine `application.properties` mit folgendem Inhalt:

```properties
spring.jdbc.url=jdbc:h2:mem:
spring.jdbc.driver=org.h2.Driver
```

Füge eine Bean für die `DataSource` hinzu und erläutere, dass sie url und driver braucht, die ja aber in den Properties definiert wurden.

```java

@Bean
public DataSource dataSource(Environment environment) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    var driver = (Driver) Class.forName(environment.getProperty("spring.jdbc.driver")).newInstance();
    var url = environment.getProperty("spring.jdbc.url");
    return new SimpleDriverDataSource(driver, Objects.requireNonNull(url));
}
```

Lade die Datasource in der main-Methode und zeige, dass sie valide ist:

```java
public static void main(String[] args)  {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MyConfig.class);
    DataSource ds = ctx.getBean(DataSource.class);
    try (Connection connection = ds.getConnection()) {
        System.out.print(connection.isValid(1000));
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
}
```

Entferne das await in der Tomcat-Config und denke daran, die DataSource-Config zu importieren.

Starte und zeige, dass es funktioniert hat.

## Conditional Datasource nach Properties

Damit die DataSource nur gestartet wird, wenn eine bestimmte Property gesetzt ist, erstelle eine passende Condition.

```java
@Bean
@Conditional(DataSourcePropertySetCondition.class)
public DataSource dataSource(Environment environment){
// ...
}
// eigene Klasse
public class DataSourcePropertySetCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return context.getEnvironment().containsProperty("spring.jdbc.url") &&
            context.getEnvironment().containsProperty("spring.jdbc.driver");
    }
}
```

Eigentlich würde man hier die URL noch auf wohlgeformtheit und den Driver auf Existenz prüfen.
Starte die Anwendung und sie sollte noch funktionieren. Entferne eine Zeile aus der `application.properties` und sie sollte nicht mehr funktionieren. Die Bean fehlt (`NoSuchBeanDefinitionException`).

## Spring Boot Source Code Walk

Importiere den Spring Boot-Sourcecode in ein neues IntelliJ-Projekt.

Zeige die spring-boot-starter. Es ist nichts in den Modulen drin außer Gradle-Dateien. Das ist also nur Dependency Management. spring-boot-starter-web hat z.B. eine Dependency auf spring-boot-starter-tomcat. Dort sind dann einige "echte" Dependencies drin, und dort ist auch die Tomcat-Core-Dependency dabei.

Zeige die Klasse `SpringBootApplication`. Erwähne, dass sie so etwas wie eine Default-Config ist. Zeige, dass sie selbst viele Annotations hat, insbesondere `ComponentScan`, wodurch alle Klassen mit einigen Filtern gescannt werden. Da im `org.springframework.autoconfiguration` sehr, sehr viel Code ist, wird entsprechend alles über den `ComponentScan` gescannt. Zeige einige Beispiele wie z.B. Flyway und Couchbase und zeige, dass es immer eine Klasse gibt, die auf `AutoConfiguration` im Namen endet.

Zeige die AutoConfiguration für `DataSource`und dass auch sie viele Annotations hat. Wir schauen erstmal auf die Classes, insbesondere die Nested Classes.
Es sind im Prinzip alles nur Spring-Konfigurationen. Schauen wir uns die `PooledDataSourceConfiguration` an. Sie hat eine Condition. Sie prüft letztendlich nur auf ein Property. `ConditionalOnProperty` ist eine Spring Boot-Erweiterung. Zeige andere, wie z.B. `ConditionalOnBean`. Besonders interessant ist `ConditionalOnMissingBean`.

In der `PooledDataSourceConfiguration` wird `ConditionalOnMissingBean` benutzt, um zu entscheiden, dass nur eine DS angelegt wird, wenn sie fehlt.
Leider werden noch sehr viele weitere Konfigurationen importiert. Für Hikari sieht man, dass es fröhlich weiter mit Annotationen geht.

Wir schauen uns nun den Tomcat an. Dieser ist im web-Paket unter Servlet. Leider ist der Support für Tomcat verteilt. `ServletWebServerFactoryAutoConfiguration` enthält einen Teil. Wir sehen den Import auf `ServletWebServerFactoryConfiguration.EmbeddedTomcat`. Diese ist Conditional ähnlich wie unsere von vorher.

Springe weiter in `TomcatServletWebServerFactory` welche von `EmbeddedTomcat` aufgerufen wird. Dort wird in `getWebServer()` indirekt der `TomcatWebServer` gestartet. In `startNonDaemonAwaitThread()` finden wir genau das await aus unserem Code.
Das ist zwar alles über viele Klassen verteilt, ist aber im Wesentlichen das gleiche wie das, was wir getan haben. Es wird aber das `DispatcherServlet` geladen statt unser Date-Servlet.

## 4 Weitere Infos

- Es ist gefährlich, einfach nur die Dependencies z.B. für Tomcat selbst hinzuzufügen, statt die Starter zu benutzen. Man hängt sich so nämlich vom Version Management von Spring ab.
