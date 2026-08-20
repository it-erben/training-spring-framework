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

# Persistenz mit Spring Data JPA

---

## In diesem Modul

* Das Problem: Objekte im Code, Tabellen in der Datenbank
* Entities: `@Entity`, `@Id`, Beziehungen mit `@ManyToOne` und `@OneToMany`
* Repositories: CRUD, Abfragen aus Methodennamen, `@Query` mit JPQL
* Transaktionen: `@Transactional`, Commit und Rollback
* H2, `ddl-auto`
* Demo: die Buchhandlung bekommt eine Datenbank

---

## Das Problem: Objekte und Tabellen

Unsere `Book`-Objekte leben bisher in einer `Map` im Service. Nach dem Neustart ist alles weg. Eine relationale Datenbank löst dieses Problem, aber die beiden Welten passen nicht direkt aufeinander:

| Objektwelt                              | Tabellenwelt                         |
|-----------------------------------------|--------------------------------------|
| Objekt mit Referenz: `book.getAuthor()` | Fremdschlüssel-Spalte: `author_id`   |
| Identität über die Referenz             | Identität über den Primärschlüssel   |
| `List<Book>` beim Autor                 | Es gibt keine Listen, nur einen Join |
| `BigDecimal`, `LocalDate`, Enums        | `numeric`, `date`, `varchar`         |

Diese Lücke heißt **Object-Relational Impedance Mismatch**.

---

<!-- _class: dense -->
## Was ein ORM löst und was nicht

**ORM** bedeutet Object-Relational Mapping: Ein Framework übernimmt die Übersetzung zwischen Objekten und Tabellen. Der Stack, mit dem wir arbeiten:

1. **JPA** (Jakarta Persistence API): der Standard. Annotationen wie `@Entity`, definiert Verhalten, liefert keine Implementierung.
2. **Hibernate** ist die verbreitetste JPA-Implementierung. Boot wählt sie als Default.
3. **Spring Data JPA** ist eine Schicht darüber mit Repositories, die uns fast allen restlichen Code abnehmen.

Was ORM **nicht** löst:

* SQL verschwindet nicht ganz, es wird nur vom ORM erzeugt.
* Falsch eingesetztes Mapping erzeugt ineffiziente Abfragemuster

---

# Entities

---

<!-- _class: densest -->
## @Entity, @Id, @GeneratedValue

Eine Entity ist eine Klasse, die auf eine Tabelle abgebildet wird. Unser `Book` aus der Demo:

```java
@Entity
public class Book {

    @Id
    @GeneratedValue
    private Long id;

    private String isbn;
    private String title;
    private BigDecimal netPrice;

    protected Book() { }  // von JPA gefordert

    public Book(String isbn, String title, BigDecimal netPrice, Author author) { ... }
}
```

* `@Entity` macht die Klasse zur Tabelle, `@Id` markiert den Primärschlüssel, `@GeneratedValue` lässt die Datenbank die Werte vergeben.
* Der technische Schlüssel `id` kommt **zusätzlich** zur fachlichen ISBN. Primärschlüssel sollen sich nie ändern.
* Anders als die Records aus Modul 02 braucht eine Entity einen **parameterlosen Konstruktor** und **veränderbare Felder**: Hibernate erzeugt Instanzen per Reflection, schreibt die Spaltenwerte direkt hinein und schreibt Änderungen zurück.

---

<!-- _class: densest -->
## Spaltennamen und Typen

Woher weiß Hibernate, wie die Tabelle aussieht? Das wird per **Konvention** abgeleitet:

```text
Hibernate: create sequence book_seq start with 1 increment by 50
Hibernate: create table book (net_price numeric(38,2), author_id bigint,
    id bigint not null, isbn varchar(255), title varchar(255), primary key (id))
```

* Klasse `Book` → Tabelle `book`, Feld `netPrice` → Spalte `net_price`. camelCase wird zu snake_case.
* Java-Typen werden auf SQL-Typen abgebildet: `BigDecimal` → `numeric`, `String` → `varchar(255)`, `Long` → `bigint`.
* Wo die Konvention nicht passt, können wir mit `@Column` das Verhalten überschreiben:

```java
// Beispiel — unsere Demo kommt mit den Konventionen aus:
@Column(name = "isbn_13", unique = true, nullable = false, length = 17)
private String isbn;
```

* `unique` und `nullable` landen als Constraints im Schema

---

<!-- _class: denser -->
## @ManyToOne

Viele Bücher gehören zu einem Autor:

```java
@Entity
public class Book {

    @ManyToOne(cascade = CascadeType.PERSIST)
    private Author author;
}
```

* Aus dem Feld `author` entsteht die Fremdschlüssel-Spalte `author_id`. Die Objektreferenz wird hier zum Join.
* Die Seite mit `@ManyToOne` **besitzt** die Beziehung: Hier liegt der Fremdschlüssel.
* `cascade = CascadeType.PERSIST`: Wer ein Buch mit einem noch nicht gespeicherten Autor speichert, speichert den Autor mit.

```text
Hibernate: alter table if exists book add constraint ...
    foreign key (author_id) references author
```

---

<!-- _class: densest -->
## @OneToMany und mappedBy

Die Gegenrichtung ist `@OneToMane`. Ein Autor kennt seine Bücher:

```java
@Entity
public class Author {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "author")
    private List<Book> books = new ArrayList<>();
}
```

* `mappedBy = "author"` verweist auf das **Feld in `Book`**: Die Beziehung ist dort schon definiert, `author_id` existiert bereits.
* Ohne `mappedBy` hielte Hibernate beide Seiten für eigenständige Beziehungen und erzeugte eine überflüssige Verknüpfungstabelle.
* Die Gegenseite ist optional. `@ManyToOne` allein reicht. Eine bidirektionale Beziehung lohnt sich nur, wenn die Navigation `author.getBooks()` fachlich gebraucht wird.

---

## FetchType: EAGER und LAZY

`FetchType` steuert, wann Beziehungen (nach-)geladen werden.

| Annotation   | Default | Bedeutet                                              |
|--------------|---------|-------------------------------------------------------|
| `@ManyToOne` | `EAGER` | Der Autor wird beim Laden des Buchs sofort mitgeladen |
| `@OneToMany` | `LAZY`  | Die Bücherliste wird erst beim ersten Zugriff geladen |

* `LAZY` heißt: Hinter `author.getBooks()` steckt zunächst ein **Proxy**. Die Abfrage läuft erst, wenn jemand die Liste wirklich braucht.
* Die Proxies sind der Grund, warum man Entities nicht direkt serialisieren sollte: Jackson fasst beim Serialisieren **jedes** Feld an und löst damit entweder Nachladeabfragen oder eine `LazyInitializationException` aus.

---

## Ausblick: das N+1-Problem

```java
bookRepository.findAll()                      // 1 Abfrage: alle Bücher
    .forEach(b -> b.getAuthor().getName());   // + N Abfragen: ein Select pro Autor?
```

* **N+1-Problem:** Eine Abfrage für die Liste, dann eine weitere pro Element für die Beziehung. Bei 1.000 Büchern 1.001 Statements statt einem Join.
* Bei fünf Demo-Büchern fällt es nicht auf, unter Last wird es zum Performanceproblem.
* `show-sql=true` macht es sichtbar

---

# Repositories

---

## Das Repository-Pattern

Die Entity beschreibt die **Daten**. Wer übernimmt den **Zugriff**? Klassischerweise ist das eine DAO-Klasse pro Entity.

Das Repository-Pattern abstrahiert den Datenzugriff hinter einem Interface und Spring Data implementiert es für uns:

```java
public interface BookRepository extends JpaRepository<Book, Long> {
}
```

* Spring Data erzeugt zur Laufzeit ein **Proxy-Objekt** mit fertigem CRUD.
* Das Interface ist eine ganz normale Bean.

---

## JpaRepository: was man geschenkt bekommt

| Methode                          | Tut                         |
|----------------------------------|-----------------------------|
| `save(entity)` / `saveAll(list)` | Einfügen oder Aktualisieren |
| `findById(id)`                   | Ein Element als `Optional`  |
| `findAll()`                      | Alle Elemente               |
| `count()`                        | Anzahl der Zeilen           |
| `deleteById(id)` / `deleteAll()` | Löschen                     |
| `existsById(id)`                 | Existenzprüfung ohne Laden  |

---

<!-- _class: denser -->
## Derived Query Methods

CRUD reicht aber selten aus. Viele Fachlichkeiten braucht Suchen. Spring Data leitet Abfragen **aus dem Methodennamen** ab. Die drei aus unserer Demo werden sein:

```java
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByTitleContainingIgnoreCase(String fragment);

    List<Book> findByNetPriceLessThan(BigDecimal limit);
}
```

| Methodenname                      | Erzeugtes SQL (sinngemäß)              |
|-----------------------------------|----------------------------------------|
| `findByIsbn`                      | `where isbn = ?`                       |
| `findByTitleContainingIgnoreCase` | `where upper(title) like upper('%…%')` |
| `findByNetPriceLessThan`          | `where net_price < ?`                  |

---

<!-- _class: densest -->
## Die Namensregeln

Das Schema ist `findBy` + Feldname + optionaler Operator. Spring Data zerlegt den Namen beim Start und baut die Abfrage daraus auf.

| Baustein                   | Beispiel                 | Bedeutung                  |
|----------------------------|--------------------------|----------------------------|
| `findBy<Feld>`             | `findByIsbn`             | Gleichheit                 |
| `Containing`               | `findByTitleContaining`  | `like '%…%'`               |
| `IgnoreCase`               | `…ContainingIgnoreCase`  | Groß-/Kleinschreibung egal |
| `LessThan` / `GreaterThan` | `findByNetPriceLessThan` | Vergleich                  |
| `Between`                  | `findByNetPriceBetween`  | Bereich, zwei Parameter    |
| `And` / `Or`               | `findByTitleAndIsbn`     | Verknüpfung                |
| `OrderBy…Desc`             | `…OrderByTitleDesc`      | Sortierung                 |

* Der Rückgabetyp ist frei wählbar: `Optional<Book>` für höchstens einen Treffer, `List<Book>` für viele.
* Wichtig: Die Feldnamen müssen **exakt** den Entity-Feldern entsprechen, sonst gibt es beim Start eine Exception.

---

## Grenzen der Ableitung

Die Ableitung ist Konvention mit Grenzen:

* **Der Name muss auf Felder abbildbar sein.** `findByFeeRange(min, max)` scheitert beim Start: Es gibt kein Feld `feeRange`.
* **Komplexe Bedingungen machen den Namen unlesbar.** `findByTitleContainingIgnoreCaseAndNetPriceLessThanOrderByTitleAsc` ist technisch gültig, aber nicht lesbar

---

## @Query mit JPQL

`@Query` an der Methode ersetzt die Namens-Ableitung durch eine explizite Abfrage:

```java
@Query("select b from Book b where b.author.name = :name")
List<Book> findByAuthorName(@Param("name") String name);
```

* Die Sprache ist **JPQL** (Jakarta Persistence Query Language): Sie sieht aus wie SQL, arbeitet aber auf **Entities und Feldern**, nicht auf Tabellen und Spalten.
* `b.author.name` navigiert über die Beziehung. Im SQL-Log wird daraus ein `join author` über die Fremdschlüssel-Spalte. Den Join schreibt Hibernate.
* Der Methodenname ist jetzt **frei wählbar**.
* Da JPQL auf dem Mapping arbeitet, bleibt die Abfrage datenbankunabhängig.

---

## Parameter binden

Werte gehören nicht in den Abfrage-String. Sie werden als Parameter gebunden:

```java
// Benannte Parameter — unsere Wahl:
@Query("select b from Book b where b.author.name = :name")
List<Book> findByAuthorName(@Param("name") String name);

// Positionsparameter — funktioniert, aber fragil bei mehreren Parametern:
@Query("select b from Book b where b.author.name = ?1")
List<Book> findByAuthorName(String name);
```

* `:name` im JPQL wird über `@Param("name")` an den Methodenparameter gebunden.
* Die Zuordnung steht nun im Code, nicht in der Reihenfolge.
* Die Bindung schützt zugleich vor **SQL-Injection**

---

# Transaktionen

---

## Warum Transaktionen?

Transaktionen verhindern, dass unvollständige Änderungen in der Datenbank verbleiben.

Der Ablauf im Kern:

1. Transaktion beginnt.
2. Beliebig viele Lese- und Schreiboperationen.
3. **Commit**: alle Änderungen werden dauerhaft. Oder **Rollback**: die Datenbank ist wieder im Zustand von Schritt 1.

---

<!-- _class: densest -->
## @Transactional

Spring modelliert Transaktionen deklarativ über eine Annotation an der Service-Methode:

```java
@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public void raisePrices(BigDecimal factor) {
        bookRepository.findAll().forEach(book -> book.setNetPrice(
                book.getNetPrice().multiply(factor).setScale(2, RoundingMode.HALF_UP)));
    }
}
```

* Beim Aufruf beginnt eine Transaktion. Am regulären Methodenende folgt der **Commit**, bei einer RuntimeException der **Rollback**.
* Auffällig: **nirgendwo steht ein `save()`**. Innerhalb der Transaktion sind geladene Entities *managed*: beim Commit erkennt Hibernate jede Änderung selbst (**Dirty Checking**) und schreibt die UPDATE-Statements:

```text
Hibernate: update book set author_id=?,isbn=?,net_price=?,title=? where id=?
```

---

## Lesende Transaktionen

Auch reine Lesezugriffe profitieren von einer Transaktionsklammer:

```java
// Beispiel — so sähe eine reine Lesemethode im BookService aus:
@Transactional(readOnly = true)
public List<Book> findAll() {
    return bookRepository.findAll();
}
```

* `readOnly = true` markiert: Hier wird nichts geändert.
* Hibernate spart sich dann das Dirty Checking. Die Snapshots der geladenen Entities entfallen, was bei großen Ergebnismengen Speicher und Zeit spart.
* Zugleich ist es Dokumentation: Die Signatur sagt dem nächsten Leser, dass diese Methode keine Schreibabsicht hat.

---

<!-- _class: dense -->
## Wo gehört @Transactional hin?

An die **Service-Schicht**, also dort, wo der fachliche Anwendungsfall definiert ist:

* **Nicht an den Controller:** HTTP-Verarbeitung und JSON-Serialisierung haben in einer Datenbanktransaktion nichts verloren.
* **Nicht ans Repository:** Ein Anwendungsfall umfasst oft mehrere Repository-Aufrufe. Die Klammer muss um **alle** liegen, sonst committet jeder Aufruf einzeln.

Spring setzt die Annotation über einen **Proxy** um (dasselbe Muster wie bei den Repositories):

* Nur **public** Methoden, die **von außen** aufgerufen werden, sind transaktional.
* Ein Aufruf `this.raisePrices(...)` innerhalb derselben Klasse läuft am Proxy vorbei. Die Annotation wirkt dann nicht.

---

# Datenbank und Konfiguration

---

<!-- _class: dense -->
## H2 für die Entwicklung

Für Entwicklung und Schulung nutzen wir **H2**, eine In-Memory-Datenbank.

```properties
spring.datasource.url=jdbc:h2:mem:bookstore
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.h2.console.enabled=true
```

* `jdbc:h2:mem:bookstore` bedeutet: die Datenbank lebt im Speicher der JVM. Start mit leerer Datenbank, Stopp löscht die Daten. Für Demos ideal, als Persistenz natürlich nicht geeignet.
* Liegt nur H2 auf dem Classpath, konfiguriert die AutoConfiguration aus Modul 01 die `DataSource` sogar ganz ohne URL.
* `show-sql=true` schreibt jedes erzeugte Statement ins Log.

---

## ddl-auto

Wie wird in unserem Beispiel das Schema erzeugt? `spring.jpa.hibernate.ddl-auto` steuert, was Hibernate beim Start mit dem Schema macht:

| Wert          | Beim Start                                      | Einsatz           |
|---------------|-------------------------------------------------|-------------------|
| `validate`    | Nur prüfen: Passt das Schema zu den Entities?   | Produktion        |
| `update`      | Fehlende Tabellen/Spalten ergänzen, nie löschen | Fragwürdig        |
| `create`      | Schema löschen und neu erzeugen                 | Frühe Entwicklung |
| `create-drop` | Wie `create`, zusätzlich löschen beim Stopp     | Demos, Tests      |

* Der Boot-Default hängt von der Datenbank ab: **embedded** (H2) → `create-drop`, alle anderen Datenbanken defaulten auf keine Schema-Verwaltung (`none`).

---

## Warum ddl-auto nicht in Produktion gehört

`update` klingt nach dem Kompromiss: Schema wächst automatisch mit. Aber das verursacht Probleme.

* **Umbenennungen versteht Hibernate nicht:** Aus `netPrice` → `price` macht `update` eine **neue Spalte** `price`. Die alte bleibt samt Daten verwaist zurück.
* **Keine automatische Migration:** Typänderungen, `not null` auf gefüllten Tabellen, Umzug von Werten - alles nicht möglich
* **Keine Historie:** Welches Schema Version 1.3 hatte, weiß niemand.
* `create`/`create-drop` in Produktion: **Datenverlust beim Start.**

Der professionelle Weg: **versionierte Migrations-Skripte**, die wie Code im Repository liegen mit **Flyway** oder **Liquibase**

---

<!-- _class: densest -->
## Umstieg auf PostgreSQL

Im POM den Treiber tauschen:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

In den Properties die Verbindung, typischerweise in einem Profil (`application-prod.properties`, Modul 01):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bookstore
spring.datasource.username=bookstore
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
```

* Entities, Repositories, JPQL, Services: **unverändert**. Hibernate erzeugt jetzt PostgreSQL-SQL.
* `ddl-auto` steht auf `validate`: das Schema kommt ab hier aus Migrationen

---

# Demo

---

# Übung

---

<!-- _class: denser -->
## Assignment 03

`assignments/sb-basics-data-jpa-assignment` liefert dieselben Bausteine, wieder die **Kursverwaltung** aus Modul 02: `Course` und `Trainer` statt `Book` und `Author`.

* Gegeben: alle Typen sind fertig: `CourseAdminApplication`, `Trainer` (fertige Entity), `Course` (noch **ohne** JPA-Annotationen), `CourseRepository`, Testdaten.
* Ausgangszustand: Die Tests sind rot, weil der Kontext nicht startet: `Not a managed type: Course`.
* Nach Aufgabe 1 startet er, und die drei Tests zu den abgeleiteten Methoden sind grün. Aufgabe 2 und 3 haben je einen eigenen Test.

Drei Aufgaben jeweils mit Tests

1. `Course` als Entity mappen
2. `findByFeeRange` mit `@Query` und `@Param` implementieren
3. `findSummaries` mit `@Query` implementieren

**Ausblick:** Modul 04 testet die Buchhandlung durch alle Schichten, inklusive `@DataJpaTest` gegen genau die Repositories von heute.
