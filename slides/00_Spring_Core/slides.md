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

# Spring Core: Der Container

---

## In diesem Modul

* Warum überhaupt ein Framework? Inversion of Control und Dependency Injection
* Der Spring-Container: `ApplicationContext`
* Beans deklarieren: `@Component`, Stereotypen, `@Configuration` und `@Bean`
* Beans injizieren: Konstruktor-Injection, `@Primary`, `@Qualifier`
* Bean Scopes und Lifecycle
* Konfigurationswerte mit `@Value`
* Maven-Grundlagen: `pom.xml`, Parent-POM, Dependencies, Reaktor
* Demo und Übung: eine kleine Buchhandlung

---

<!-- _class: dense -->
## Das Problem: Abhängigkeiten selbst auflösen

Ohne Framework bauen wir unseren Objektgraphen von Hand zusammen:

```java
BookRepository repository = new InMemoryBookRepository();
PriceCalculator gross = new GrossPriceCalculator();
PriceCalculator net = new NetPriceCalculator();
BookService service = new BookService(repository, gross, net);
```

Das funktioniert — skaliert aber schlecht:

* Jede Klasse, die `BookService` braucht, muss wissen, **wie** man ihn baut.
* Ändert sich ein Konstruktor, bricht Code an vielen Stellen.
* Austausch einer Implementierung (z.B. echte DB statt In-Memory) heißt: überall `new` anfassen.
* Querschnittsthemen (Lebenszyklus, Konfiguration, Proxies) landen im Fachcode.

---

## Inversion of Control

**Idee:** Nicht die Klasse besorgt sich ihre Abhängigkeiten, ein Container tut das.

* Klassisch: Der Code ruft Bibliotheken auf und kontrolliert den Ablauf.
* IoC: Das Framework kontrolliert den Ablauf und ruft unseren Code auf ("Hollywood-Prinzip: *Don't call us, we call you*").
* Der Spring-Container übernimmt:
  * **Erzeugen** der Objekte (Beans)
  * **Injizieren** der Abhängigkeiten untereinander
  * **Verwalten** des Lebenszyklus (Erzeugung bis Zerstörung)
* Unser Fachcode deklariert nur noch, **was** er braucht, nicht **woher** es kommt.

---

<!-- _class: dense -->
## Dependency Injection

Dependency Injection (DI) ist die konkrete Technik hinter IoC:

* Eine Klasse bekommt ihre Abhängigkeiten **von außen hineingereicht** statt sie mit `new` selbst zu erzeugen.
* `BookService` hängt nur vom Interface `BookRepository` ab:

```java
public interface BookRepository {

    List<Book> findAll();

    Optional<Book> findByIsbn(String isbn);
}
```

* Welche Implementierung zur Laufzeit dahintersteckt, entscheidet der Container.
* Gewinn: **lose Kopplung**. Implementierungen sind austauschbar, Tests können Fakes injizieren.

---

<!-- _class: denser -->
## Der ApplicationContext

Der `ApplicationContext` ist der Spring-Container:

* Er kennt alle Bean-Definitionen (welche Klassen, welche Scopes, welche Abhängigkeiten).
* Beim Start erzeugt er die Beans in der richtigen Reihenfolge und verbindet sie.
* In Spring Boot startet ihn eine Zeile:

```java
@SpringBootApplication
public class CoreDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreDemoApplication.class, args);
    }
}
```

* `SpringApplication.run(...)` baut den Context auf, führt danach z.B. `CommandLineRunner`-Beans aus und gibt den Context zurück, falls man ihn braucht.

---

# Beans deklarieren

---

<!-- _class: densest -->
## @Component und die Stereotypen

Eine **Bean** ist ein Objekt, das der Container erzeugt und verwaltet. Der einfachste Weg dorthin: eine Stereotyp-Annotation an der Klasse.

```java
@Repository
public class InMemoryBookRepository implements BookRepository {
    // ...
}
```

| Annotation                        | Bedeutung                                |
|-----------------------------------|------------------------------------------|
| `@Component`                      | Generische Bean. Basis aller Stereotypen |
| `@Service`                        | Fachlogik / Geschäftsdienste             |
| `@Repository`                     | Datenzugriff (+ Exception-Übersetzung)   |
| `@Controller` / `@RestController` | Web-Schicht (Modul Web)                  |

Technisch verhalten sich alle gleich. Die Spezialisierungen dokumentieren die **Rolle** der Klasse und schalten teils Zusatzverhalten frei.

---

<!-- _class: denser -->
## Component Scan

Woher weiß der Container, welche Klassen Beans sind? Er **scannt den Classpath**:

```java
@SpringBootApplication
public class CoreDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreDemoApplication.class, args);
    }
}
```

* `@SpringBootApplication` enthält u.a. `@ComponentScan`.
* Gescannt wird ab dem **Package der Application-Klasse**, inklusive aller Sub-Packages.
* Konvention: Application-Klasse ins **Root-Package** legen (hier `tech.erben.springboot.basics.core`), dann wird alles darunter gefunden.
* Klassen außerhalb dieses Baums werden nicht gefunden, eine häufige Fehlerquelle: `NoSuchBeanDefinitionException` beim Start.

---

<!-- _class: dense -->
## @Configuration und @Bean

Nicht jede Klasse können wir annotieren, z.B. Klassen aus dem JDK oder aus Fremdbibliotheken. Dafür gibt es `@Bean`-Methoden:

```java
@Configuration
public class ShippingConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
```

* `@Configuration` markiert eine Klasse, die Bean-Definitionen enthält.
* Jede `@Bean`-Methode liefert eine Bean; der Methodenname wird zum Bean-Namen (`clock`).
* Der Rückgabewert wird vom Container verwaltet wie jede `@Component`-Bean.

---

## Wann @Component, wann @Bean?

| Situation                                              | Mittel der Wahl             |
|--------------------------------------------------------|-----------------------------|
| Eigene Klasse, eine Implementierung                    | `@Component` / Stereotyp    |
| Fremde Klasse (JDK, Library)                           | `@Bean`-Methode             |
| Aufwendige Konstruktion (Builder, Parameter)           | `@Bean`-Methode             |
| Mehrere Varianten derselben Klasse                     | Mehrere `@Bean`-Methoden    |
| Bean nur unter Bedingungen (später: AutoConfiguration) | `@Bean` + `@Conditional...` |

**Faustregel:** Eigener Code → Annotation an der Klasse. Fremder Code oder Konstruktionslogik → `@Bean`-Methode in einer `@Configuration`.

---

# Beans injizieren

---

<!-- _class: denser -->
## Konstruktor-Injection

Der empfohlene Weg: Abhängigkeiten als **Konstruktor-Parameter** deklarieren.

```java
@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository, /* ... */) {
        this.bookRepository = bookRepository;
        // ...
    }
}
```

* Der Container sieht: `BookService` braucht ein `BookRepository` und injiziert die passende Bean (`InMemoryBookRepository`).
* Seit Spring 4.3: Bei genau einem Konstruktor ist kein `@Autowired` nötig.
* Felder können `final` sein. Das Objekt ist nach der Konstruktion vollständig und unveränderlich.

---

<!-- _class: denser -->
## Warum nicht Field-Injection?

```java
// So bitte nicht:
@Autowired
private BookRepository bookRepository;
```

Drei konkrete Gründe dagegen:

1. **Testbarkeit ohne Container:** Im Unit-Test lässt sich die Abhängigkeit nicht einfach übergeben, man braucht Reflection oder gleich den ganzen Spring-Context.
2. **Keine `final`-Felder:** Das Feld muss nach der Konstruktion beschreibbar bleiben. Unveränderlichkeit und garantierte Initialisierung gehen verloren.
3. **Versteckte Abhängigkeiten:** `new BookService()` kompiliert, liefert aber ein nicht voll funktionsfähiges Objekt. Beim Konstruktor sieht man alle Abhängigkeiten auf einen Blick (und merkt, wenn es zu viele werden).

**Merksatz:** Konstruktor-Injection ist der Default. Setter-Injection nur für wirklich optionale, veränderbare Abhängigkeiten.

---

<!-- _class: densest -->
## Mehrere Kandidaten: @Primary

Was, wenn **zwei** Beans dasselbe Interface implementieren?

```java
public interface PriceCalculator {

    BigDecimal calculate(Book book);
}
```

Ohne weitere Angabe scheitert der Start: `NoUniqueBeanDefinitionException`. Erste Lösung: eine Bean zum **Standard** erklären:

```java
@Component("grossPriceCalculator")
@Primary
public class GrossPriceCalculator implements PriceCalculator {

    @Override
    public BigDecimal calculate(Book book) {
        return book.netPrice().multiply(VAT_FACTOR).setScale(2, RoundingMode.HALF_UP);
    }
}
```

Jede unqualifizierte Injektion von `PriceCalculator` erhält jetzt den `GrossPriceCalculator`.

---

<!-- _class: densest -->
## Mehrere Kandidaten: @Qualifier

Zweite Lösung: die gewünschte Bean **beim Injizieren benennen**. Beide Wege im selben Konstruktor:

```java
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final PriceCalculator defaultCalculator;
    private final PriceCalculator netCalculator;

    public BookService(BookRepository bookRepository,
                       PriceCalculator defaultCalculator,
                       @Qualifier("netPriceCalculator") PriceCalculator netCalculator) {
        this.bookRepository = bookRepository;
        this.defaultCalculator = defaultCalculator;
        this.netCalculator = netCalculator;
    }
}
```

* `defaultCalculator` trägt keinen Qualifier → `@Primary` greift → `GrossPriceCalculator`.
* `netCalculator` wählt per `@Qualifier("netPriceCalculator")` explizit den `NetPriceCalculator`. Der Name stammt aus `@Component("netPriceCalculator")`.

---

<!-- _class: denser -->
## Optionale Abhängigkeiten

Nicht jede Abhängigkeit muss zwingend existieren oder als Singleton vorliegen:

```java
public CatalogRunner(BookService bookService,
                     ShopProperties shopProperties,
                     Clock clock,
                     ObjectProvider<PrototypeCounter> counterProvider) {
    // ...
}
```

* `ObjectProvider<T>` injiziert eine **Factory** statt der Bean selbst:
  * `getObject()`: Bean holen (bei Prototype: **jedes Mal eine neue**, gleich mehr dazu)
  * `getIfAvailable()`: `null`-frei mit Fallback arbeiten, wenn die Bean fehlt
* Alternativen für "darf fehlen": `Optional<T>` als Parametertyp oder `@Autowired(required = false)` (Setter).
* Auch möglich: `List<PriceCalculator>` injiziert **alle** Implementierungen auf einmal.

---

# Bean Scopes und Lifecycle

---

## Singleton (Default)

* **Eine Instanz pro Container**, nicht pro JVM, nicht pro Aufruf.
* Jede Injektion von `BookService` liefert **dasselbe** Objekt.
* Default für alle Beans; keine Annotation nötig.
* Konsequenz: Singleton-Beans sollten **zustandslos** (stateless) sein oder ihren Zustand thread-sicher verwalten. Viele Threads teilen sich dieselbe Instanz.
* Erzeugung standardmäßig **eager** beim Container-Start: fehlende oder mehrdeutige Abhängigkeiten fallen sofort auf, nicht erst beim ersten Request.

---

<!-- _class: denser -->
## Prototype

`@Scope("prototype")`: Der Container liefert bei **jeder Anfrage eine neue Instanz**.

```java
@Component
@Scope("prototype")
public class PrototypeCounter {

    private static final AtomicInteger INSTANCES = new AtomicInteger();

    private final int instanceNumber = INSTANCES.incrementAndGet();

    public int instanceNumber() {
        return instanceNumber;
    }
}
```

```java
PrototypeCounter first = counterProvider.getObject();
PrototypeCounter second = counterProvider.getObject();
// first != second — zwei verschiedene Instanzen
```

**Achtung:** In eine Singleton-Bean direkt injiziert, würde der Prototype nur **einmal** erzeugt, deshalb der `ObjectProvider`.

---

## Request und Session (nur erwähnt)

* In Web-Anwendungen gibt es weitere Scopes: **`request`** (eine Instanz pro HTTP-Request) und **`session`** (eine pro HTTP-Session).
* Für dieses Modul genügt: Es gibt sie, sie brauchen einen Web-Context, wir kommen im Web-Modul darauf zurück.
* Merken: In der Praxis sind **> 95 % aller Beans Singletons**.

---

<!-- _class: denser -->
## @PostConstruct und @PreDestroy

Der Container verwaltet den **Lebenszyklus** und bietet Hooks an:

```java
@PostConstruct
void initialize() {
    // läuft einmal, nachdem alle Abhängigkeiten injiziert sind
}

@PreDestroy
void shutdown() {
    // läuft beim Herunterfahren des Containers
}
```

* `@PostConstruct`: Initialisierung, die injizierte Abhängigkeiten braucht. Im Konstruktor wäre das zu früh, wenn z.B. Proxies noch fehlen.
* `@PreDestroy`: Ressourcen freigeben (Verbindungen, Threads, Dateien).
* Beide stammen aus `jakarta.annotation`. Standard, nicht Spring-spezifisch.
* Bei **Prototype**-Beans ruft der Container `@PreDestroy` **nicht** auf. Er verwaltet sie nach der Erzeugung nicht weiter.

---

# Konfigurationswerte

---

<!-- _class: dense -->
## @Value

Werte aus `application.properties` in Beans injizieren:

```properties
shop.name=Buchhandlung Erben
```

```java
@Component
public class ShopProperties {

    @Value("${shop.name:Buchhandlung Erben}")
    private String name;

    public String getName() {
        return name;
    }
}
```

* `${...}` ist ein Platzhalter. Der Container ersetzt ihn beim Erzeugen der Bean.
* Quellen: `application.properties`, Umgebungsvariablen, Kommandozeile u.v.m.

---

<!-- _class: dense -->
## Defaults und Platzhalter

* Syntax: `${property.name:defaultwert}`. Der Teil nach dem Doppelpunkt greift, wenn die Property fehlt.
* Ohne Default und ohne Wert: Der Start schlägt fehl (`Could not resolve placeholder`). Das ist oft gewollt: fail fast statt stiller Fehlkonfiguration.
* Typkonvertierung übernimmt Spring: `@Value("${shop.max-results:10}") int maxResults` funktioniert sofort.
* Platzhalter funktionieren auch in `@Bean`-Methoden-Parametern und in `application.properties` selbst (Verschachtelung).
* **Ausblick:** Für viele zusammengehörige Werte gibt es `@ConfigurationProperties` (typsichere Objekte statt einzelner `@Value`-Felder) - Thema in Modul *11_Configuration*.

---

# Maven-Grundlagen

---

<!-- _class: densest -->
## Aufbau einer pom.xml

Die `pom.xml` (*Project Object Model*) beschreibt ein Maven-Projekt:

```xml
<project>
    <modelVersion>4.0.0</modelVersion>

    <groupId>tech.erben</groupId>
    <artifactId>sb-basics-core-demo-finished</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <dependencies>
        <!-- Bibliotheken, die das Projekt braucht -->
    </dependencies>

    <build>
        <plugins>
            <!-- Werkzeuge für den Build (Compiler, Packaging, ...) -->
        </plugins>
    </build>
</project>
```

* **Koordinaten** `groupId:artifactId:version` identifizieren jedes Artefakt eindeutig, auch jede Dependency.
* Maven lädt Dependencies aus **Repositories** (zentral: Maven Central) und legt sie im lokalen Cache ab (`~/.m2/repository`).

---

<!-- _class: dense -->
## Parent-POM und Vererbung

POMs können erben. Das Kind übernimmt Konfiguration und Versionen vom Parent:

```xml
<parent>
    <groupId>tech.erben</groupId>
    <artifactId>sb-basics-core-demo</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

* Typischerweise geerbt: Java-Version (bei uns **21**), Plugin-Versionen, gemeinsame Properties.
* Spring-Boot-Projekte nutzen oft `spring-boot-starter-parent` als Parent. Unser Kurs-Repo hat einen eigenen Parent, der nur die Property `spring-boot.version` liefert.

---

<!-- _class: dense -->
## Versionskatalog importieren

Ohne `spring-boot-starter-parent` importiert jedes Modul Springs Versionskatalog selbst in seinem eigenen POM:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version> <!-- 4.0.2 -->
            <scope>import</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</dependencyManagement>
```

* `dependencyManagement` legt Versionen fest, lädt aber selbst noch nichts in den Classpath.

---

<!-- _class: denser -->
## Eine Dependency aufnehmen

Um eine neue Dependency hinzuzufügen, kommt ein Eintrag unter `<dependencies>`:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

* **Keine Version nötig**: sie wird aus dem importierten `spring-boot-dependencies`-Katalog übernommen.
* `<scope>test</scope>`: Dependency erscheint nur im Test-Classpath und landet nicht im ausgelieferten Artefakt.

---

## Starter und transitive Abhängigkeiten

* **Starter** sind kuratierte Dependency-Bündel: `spring-boot-starter` bringt Container, Logging und AutoConfiguration; `spring-boot-starter-web` später den ganzen Web-Stack.
* Transitive Abhängigkeiten löst Maven automatisch auf: Wer einen Starter aufnimmt, bekommt dessen Abhängigkeiten mit, ohne sie zu nennen.
* Sichtbar mit `mvn dependency:tree`. Der Baum zeigt auch, woher eine Version stammt, wenn zwei Zweige dieselbe Bibliothek fordern.

---

<!-- _class: denser -->
## Der Reaktor

Der **Reaktor** ist Mavens Mechanismus für Multi-Modul-Builds:

```xml
<packaging>pom</packaging>

<modules>
    <module>sb-basics-core-demo-start</module>
    <module>sb-basics-core-demo-finished</module>
</modules>
```

* Ein Aggregator-POM (`<packaging>pom</packaging>`) listet seine Module.
* `mvn install` im Wurzelverzeichnis baut **alle Module**. Der Reaktor berechnet die Reihenfolge aus den Abhängigkeiten zwischen den Modulen.
* Nützliche Flags:
  * `mvn -pl demos/sb-basics-core-demo`: nur dieses Modul bauen
  * `-am` (*also make*): dessen Abhängigkeiten im Repo mitbauen
* Unser Kurs-Repo ist genau so aufgebaut: ein Wurzel-Reaktor über `demos`, `assignments` und `solutions`.

---

# Demo

---

<!-- _class: denser -->
## Was wir gleich implementieren

Eine kleine Buchhandlung als Konsolenanwendung mit allem aus diesem Modul in Aktion:

| Baustein                                                    | Zeigt                                              |
|-------------------------------------------------------------|----------------------------------------------------|
| `Book` (Record), `BookRepository`, `InMemoryBookRepository` | Interface + `@Repository`-Bean                     |
| `BookService`                                               | Konstruktor-Injection, `@Primary` vs. `@Qualifier` |
| `GrossPriceCalculator`, `NetPriceCalculator`                | Zwei Beans, ein Interface                          |
| `ShippingConfig`                                            | `@Bean`-Methode für eine JDK-Klasse (`Clock`)      |
| `ShopProperties`                                            | `@Value` mit Default                               |
| `PrototypeCounter`, `CatalogRunner`                         | Prototype-Scope, `CommandLineRunner`               |

---

## Erwartete Ausgabe

Der Katalog erscheint mit Brutto- und Nettopreisen (19 % MwSt., kaufmännisch gerundet). Die letzte Zeile weist nach, dass der Prototype-Scope **zwei verschiedene Instanzen** liefert, der Singleton-Scope dagegen zweimal dieselbe.

Code: `demos/sb-basics-core-demo`. Der Ordner `-start` ist der Ausgangspunkt zum Mitbauen, `-finished` die Referenz.

---

# Übung

---

<!-- _class: denser -->
## Assignment 00

`assignments/sb-basics-core-assignment`: eine **Kursverwaltung** nach demselben Muster:

1. Stereotyp-Annotationen an den vorbereiteten Klassen ergänzen (Katalog, Kalkulatoren).
2. Eine Fremdklasse (`TrainerDirectory`) per `@Bean`-Methode bereitstellen.
3. Konstruktor-Injection im `CourseService` implementieren.
4. Die Mehrdeutigkeit zwischen zwei `FeeCalculator`-Beans mit `@Primary` und `@Qualifier` auflösen.

Die mitgelieferten Tests zeigen, wann ihr fertig seid: `mvn test -DskipAssignmentTests=false` muss grün sein.

**Ausblick:** Modul *11_Configuration* vertieft, wie Spring Boot per **AutoConfiguration** viele dieser Beans automatisch konfiguriert und wie man eigene AutoConfigurations und Starter baut.
