---
marp: true
theme: default
header: Spring Boot Basics
footer: Alexander Erben
paginate: true
---

# Persistenz mit Spring Data JPA

---

## In diesem Modul

* Das Problem: Objekte im Code, Tabellen in der Datenbank
* Entities: `@Entity`, `@Id`, Beziehungen mit `@ManyToOne` und `@OneToMany`
* Repositories: CRUD geschenkt, Abfragen aus Methodennamen, `@Query` mit JPQL
* Transaktionen: `@Transactional`, Commit und Rollback
* H2, `ddl-auto` und der Weg zu einer echten Datenbank
* Demo: die Buchhandlung bekommt eine Datenbank — Kurs-Stand: **Spring Boot 4.0.2, Java 21**

Modul 02 hat es versprochen: Entities tragen Persistenz-Details, die nicht ins JSON gehören. Heute sehen wir, welche das sind — und warum die DTO-Trennung sich auszahlt.

---

## Das Problem: Objekte und Tabellen

Unsere `Book`-Objekte leben bisher in einer `Map` im Service — nach dem Neustart ist alles weg. Eine relationale Datenbank löst das, aber die beiden Welten passen nicht direkt aufeinander:

| Objektwelt | Tabellenwelt |
| --- | --- |
| Objekt mit Referenz: `book.getAuthor()` | Fremdschlüssel-Spalte: `author_id` |
| Identität über die Referenz | Identität über den Primärschlüssel |
| `List<Book>` beim Autor | Es gibt keine Listen — nur einen Join |
| `BigDecimal`, `LocalDate`, Enums | `numeric`, `date`, `varchar` |

* Diese Lücke heißt **Object-Relational Impedance Mismatch**.
* Von Hand überbrücken heißt JDBC: `Connection`, SQL-Strings, `ResultSet` Spalte für Spalte in Objekte kopieren — für jede Klasse, in beide Richtungen.

---

## Was ORM löst — und was nicht

**ORM** — Object-Relational Mapping: Ein Framework übernimmt die Übersetzung zwischen Objekten und Tabellen. Der Stack, mit dem wir arbeiten:

1. **JPA** (Jakarta Persistence API): der Standard — Annotationen wie `@Entity`, definiert Verhalten, liefert keine Implementierung.
2. **Hibernate**: die verbreitetste JPA-Implementierung — erzeugt das SQL. Boot wählt sie als Default.
3. **Spring Data JPA**: eine Schicht darüber — Repositories, die uns fast allen restlichen Code abnehmen.

Was ORM **nicht** löst:

* SQL verschwindet nicht — es wird nur erzeugt. Wer es nicht lesen kann, kann nicht beurteilen, was seine Anwendung tut. Deshalb heute durchgehend `show-sql=true`.
* Falsch eingesetztes Mapping erzeugt katastrophale Abfragemuster — dazu am Ende des Entity-Teils ein Ausblick.

---

# Entities

---

## @Entity, @Id, @GeneratedValue

Eine Entity ist eine Klasse, die auf eine Tabelle abgebildet wird — unser `Book` aus der Demo:

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
* Der technische Schlüssel `id` kommt **zusätzlich** zur fachlichen ISBN — Primärschlüssel sollen sich nie ändern, Fachdaten tun es manchmal doch.
* Anders als die Records aus Modul 02 braucht eine Entity einen **parameterlosen Konstruktor** und **veränderbare Felder**: Hibernate erzeugt Instanzen per Reflection, schreibt die Spaltenwerte direkt hinein und schreibt Änderungen zurück. Genau das sind die versprochenen **Persistenz-Details** — die Entity gehört Hibernate, das DTO bleibt der Vertrag nach außen.

---

## Spaltennamen und Typen

Woher weiß Hibernate, wie die Tabelle aussieht? **Konvention** — wie überall in Boot:

```text
Hibernate: create sequence book_seq start with 1 increment by 50
Hibernate: create table book (net_price numeric(38,2), id bigint not null,
    author_id bigint, isbn varchar(255), title varchar(255), primary key (id))
```

* Klasse `Book` → Tabelle `book`, Feld `netPrice` → Spalte `net_price` — camelCase wird zu snake_case.
* Java-Typen werden auf SQL-Typen abgebildet: `BigDecimal` → `numeric`, `String` → `varchar(255)`, `Long` → `bigint`.
* Wo die Konvention nicht passt, übersteuert `@Column`:

```java
// Beispiel — unsere Demo kommt mit den Konventionen aus:
@Column(name = "isbn_13", unique = true, nullable = false, length = 17)
private String isbn;
```

* `unique` und `nullable` landen als Constraints im Schema — die Datenbank prüft mit.

---

## @ManyToOne

Beziehungen sind der Kern des Mappings. Viele Bücher gehören zu einem Autor:

```java
@Entity
public class Book {

    @ManyToOne(cascade = CascadeType.PERSIST)
    private Author author;
}
```

* Aus dem Feld `author` entsteht die Fremdschlüssel-Spalte `author_id` — die Objektreferenz wird zum Join.
* Die Seite mit `@ManyToOne` **besitzt** die Beziehung: Hier liegt der Fremdschlüssel.
* `cascade = CascadeType.PERSIST`: Wer ein Buch mit einem noch nicht gespeicherten Autor speichert, speichert den Autor mit — in der Demo spart das beim Seeding die separaten `save`-Aufrufe für die Autoren.

```text
Hibernate: alter table if exists book add constraint ...
    foreign key (author_id) references author
```

---

## @OneToMany und mappedBy

Die Gegenrichtung — ein Autor kennt seine Bücher:

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
* Die Gegenseite ist optional: `@ManyToOne` allein reicht. Eine bidirektionale Beziehung lohnt nur, wenn die Navigation `author.getBooks()` fachlich gebraucht wird.

---

## FetchType: EAGER und LAZY

Wann lädt Hibernate die Beziehung mit? Das steuert der `FetchType` — hier nur der Blick auf die Defaults:

| Annotation | Default | Bedeutet |
| --- | --- | --- |
| `@ManyToOne` | `EAGER` | Der Autor wird beim Laden des Buchs sofort mitgeladen |
| `@OneToMany` | `LAZY` | Die Bücherliste wird erst beim ersten Zugriff geladen |

* `LAZY` heißt: Hinter `author.getBooks()` steckt zunächst ein **Proxy** — die Abfrage läuft erst, wenn jemand die Liste wirklich anfasst.
* Genau diese Proxies sind der zweite Grund aus Modul 02, Entities nicht zu serialisieren: Jackson fasst beim Serialisieren **jedes** Feld an — und löst damit entweder Nachladeabfragen oder eine `LazyInitializationException` aus.
* Die bewusste Steuerung des Ladeverhaltens ist Stoff des Aufbaumoduls *13_Data* — heute reicht: Es gibt zwei Modi, und die Defaults stehen oben.

---

## Ausblick: das N+1-Problem

Einen Namen nehmen wir trotzdem schon mit, weil er in jedem JPA-Projekt fällt:

```java
bookRepository.findAll()                      // 1 Abfrage: alle Bücher
    .forEach(b -> b.getAuthor().getName());   // + N Abfragen: ein Select pro Autor?
```

* **N+1-Problem:** Eine Abfrage für die Liste, dann eine weitere pro Element für die Beziehung — bei 1.000 Büchern 1.001 Statements statt einem Join.
* Es entsteht schleichend: Bei fünf Demo-Büchern fällt es nicht auf, unter Last wird es zum Performance-Killer.
* `show-sql=true` macht es sichtbar — ein Grund mehr, das SQL-Log lesen zu können.
* Diagnose und Gegenmittel (Fetch Joins, `@EntityGraph`) gehören zum Aufbaumodul *13_Data*. Für heute genügt: Es existiert, und man erkennt es im Log.

---

# Repositories

---

## Das Repository-Pattern

Die Entity beschreibt die **Daten** — wer übernimmt den **Zugriff**? Klassisch: eine DAO-Klasse pro Entity, handgeschrieben, überall fast identisch.

Das Repository-Pattern abstrahiert den Datenzugriff hinter einem Interface — und Spring Data implementiert es für uns:

```java
public interface BookRepository extends JpaRepository<Book, Long> {
}
```

* Kein einziges Implementierungsdetail — Spring Data erzeugt zur Laufzeit ein **Proxy-Objekt** mit fertigem CRUD.
* Die Typparameter: die Entity (`Book`) und der Typ ihres `@Id`-Feldes (`Long`).
* Das Interface ist eine ganz normale Bean — der Component Scan aus Modul 00 findet es, Konstruktor-Injection funktioniert wie immer.

**Merksatz:** Wir deklarieren, *was* wir brauchen. *Wie* es passiert, ist Sache von Spring Data.

---

## JpaRepository: was man geschenkt bekommt

Das leere Interface kann bereits all das — geerbt aus `JpaRepository`:

| Methode | Tut |
| --- | --- |
| `save(entity)` / `saveAll(list)` | Einfügen oder Aktualisieren |
| `findById(id)` | Ein Element als `Optional` |
| `findAll()` | Alle Elemente |
| `count()` | Anzahl der Zeilen |
| `deleteById(id)` / `deleteAll()` | Löschen |
| `existsById(id)` | Existenzprüfung ohne Laden |

* `findById` liefert `Optional` — dieselbe Absicherung gegen "nicht gefunden", die wir aus Modul 02 kennen.
* In der Demo seeden wir mit `saveAll` fünf Bücher und prüfen mit `count()` — ohne eine Zeile SQL geschrieben zu haben.

---

## Derived Query Methods

CRUD reicht selten — Fachlichkeit braucht Suchen. Spring Data leitet Abfragen **aus dem Methodennamen** ab. Die drei aus unserer Demo:

```java
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByTitleContainingIgnoreCase(String fragment);

    List<Book> findByNetPriceLessThan(BigDecimal limit);
}
```

| Methodenname | Erzeugtes SQL (sinngemäß) |
| --- | --- |
| `findByIsbn` | `where isbn = ?` |
| `findByTitleContainingIgnoreCase` | `where upper(title) like upper('%…%')` |
| `findByNetPriceLessThan` | `where net_price < ?` |

* Keine Implementierung, keine Annotation — der Name **ist** die Abfrage.

---

## Die Namensregeln

Das Schema: `findBy` + Feldname + optionaler Operator — Spring Data zerlegt den Namen beim Start und baut die Abfrage daraus:

| Baustein | Beispiel | Bedeutung |
| --- | --- | --- |
| `findBy<Feld>` | `findByIsbn` | Gleichheit |
| `Containing` | `findByTitleContaining` | `like '%…%'` |
| `IgnoreCase` | `…ContainingIgnoreCase` | Groß-/Kleinschreibung egal |
| `LessThan` / `GreaterThan` | `findByNetPriceLessThan` | Vergleich |
| `Between` | `findByNetPriceBetween` | Bereich, zwei Parameter |
| `And` / `Or` | `findByTitleAndIsbn` | Verknüpfung |
| `OrderBy…Desc` | `…OrderByTitleDesc` | Sortierung |

* Der Rückgabetyp ist frei wählbar: `Optional<Book>` für höchstens einen Treffer, `List<Book>` für viele.
* Wichtig: Die Feldnamen müssen **exakt** den Entity-Feldern entsprechen — sonst gibt es beim Start eine Exception. Ein Tippfehler fällt also sofort auf, nicht erst zur Laufzeit der Abfrage.

---

## Grenzen der Ableitung

Die Ableitung ist Konvention, keine Magie — und sie hat klare Grenzen:

* **Der Name muss auf Felder abbildbar sein.** `findByFeeRange(min, max)` scheitert beim Start: Es gibt kein Feld `feeRange`. Fachbegriffe, die keine Felder sind, kann kein Parser erraten — dieses Beispiel begegnet euch in der Übung wieder.
* **Komplexe Bedingungen machen den Namen unlesbar.** `findByTitleContainingIgnoreCaseAndNetPriceLessThanOrderByTitleAsc` ist technisch gültig — und ein Wartungsproblem.
* **Alles jenseits von "Zeilen filtern"** — Aggregate, berechnete Werte, Projektionen auf einzelne Spalten — passt nicht ins Namensschema.

Für all das gibt es den nächsten Schritt: die Abfrage **selbst schreiben**, ohne das Repository-Modell zu verlassen.

---

## @Query mit JPQL

`@Query` an der Methode ersetzt die Namens-Ableitung durch eine explizite Abfrage — unser Beispiel aus der Demo:

```java
@Query("select b from Book b where b.author.name = :name")
List<Book> findByAuthorName(@Param("name") String name);
```

* Die Sprache ist **JPQL** (Jakarta Persistence Query Language): Sie sieht aus wie SQL, arbeitet aber auf **Entities und Feldern**, nicht auf Tabellen und Spalten — `Book` und `author`, nicht `book` und `author_id`.
* `b.author.name` navigiert über die Beziehung. Im SQL-Log wird daraus ein `join author` über die Fremdschlüssel-Spalte — den Join schreibt Hibernate.
* Der Methodenname ist jetzt **frei wählbar** — er dokumentiert die Absicht, die Annotation definiert die Abfrage.
* Da JPQL auf dem Mapping arbeitet, bleibt die Abfrage datenbankunabhängig — dazu später mehr beim Umstieg auf PostgreSQL.

---

## Parameter binden

Werte gehören **nie** in den Abfrage-String — sie werden als Parameter gebunden:

```java
// Benannte Parameter — unsere Wahl:
@Query("select b from Book b where b.author.name = :name")
List<Book> findByAuthorName(@Param("name") String name);

// Positionsparameter — funktioniert, aber fragil bei mehreren Parametern:
@Query("select b from Book b where b.author.name = ?1")
List<Book> findByAuthorName(String name);
```

* `:name` im JPQL wird über `@Param("name")` an den Methodenparameter gebunden.
* Benannte Parameter sind bei mehr als einem Parameter klar im Vorteil: Die Zuordnung steht im Code, nicht in der Reihenfolge.
* Die Bindung schützt zugleich vor **SQL-Injection** — der Wert wird als Datum übergeben, nie in den Abfragetext eingebaut. String-Konkatenation in Abfragen ist tabu.

---

# Transaktionen

---

## Warum Transaktionen?

Unsere Demo hebt alle Buchpreise um zehn Prozent an — fünf UPDATE-Statements. Was, wenn nach dem dritten der Prozess stirbt?

* Ohne Transaktion: drei Bücher teurer, zwei nicht — ein **inkonsistenter Zustand**, den niemand bestellt hat und niemand bemerkt.
* Mit Transaktion gilt **alles oder nichts**: Entweder alle fünf Preise ändern sich, oder keiner.

Der Ablauf im Kern:

1. Transaktion beginnt.
2. Beliebig viele Lese- und Schreiboperationen.
3. **Commit** — alle Änderungen werden dauerhaft. Oder **Rollback** — die Datenbank ist wieder im Zustand von Schritt 1.

* Datenbanken können das seit Jahrzehnten. Die Frage ist nur, wer Beginn, Commit und Rollback im Code auslöst — von Hand ist das fehleranfälliger Boilerplate.

---

## @Transactional

Spring erledigt das deklarativ — eine Annotation an der Service-Methode:

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

* Beim Aufruf beginnt eine Transaktion, am normalen Methodenende folgt der **Commit**, bei einer RuntimeException der **Rollback**.
* Auffällig: **nirgendwo ein `save()`**. Innerhalb der Transaktion sind geladene Entities *managed* — beim Commit erkennt Hibernate jede Änderung selbst (**Dirty Checking**) und schreibt die UPDATE-Statements:

```text
Hibernate: update book set author_id=?,isbn=?,net_price=?,title=? where id=?
```

* Die Maschinerie dahinter (Persistence Context, `EntityManager`) öffnet das Aufbaumodul *13_Data*.

---

## Rollback: wann automatisch?

Die Regel ist präziser, als man denkt — und die Präzision ist prüfungsrelevant für jeden Produktions-Bug:

| Es fliegt … | Rollback? |
| --- | --- |
| `RuntimeException` (z. B. `IllegalStateException`) | **Ja**, automatisch |
| `Error` | **Ja**, automatisch |
| Checked Exception (z. B. `IOException`) | **Nein** — es wird committet! |

* Die Demo führt es vor: `raisePricesAndFail` ändert alle Preise, ruft `flush()` — die UPDATEs erscheinen im SQL-Log — und wirft dann eine `IllegalStateException`:

```text
>>> IllegalStateException gefangen: Absichtlicher Fehler nach der Preisänderung …
>>> Preise nach raisePricesAndFail(2.00) — unverändert dank Rollback: [49.49, ...]
```

* Trotz ausgeführter UPDATEs: Nach dem Rollback stehen die alten Preise in der Datenbank.
* Wer bei einer checked Exception zurückrollen will, sagt es explizit: `@Transactional(rollbackFor = IOException.class)`.

---

## Lesende Transaktionen

Auch reine Lesezugriffe profitieren von einer Transaktionsklammer — mit einem Hinweis an die Infrastruktur:

```java
// Beispiel — so sähe eine reine Lesemethode im BookService aus:
@Transactional(readOnly = true)
public List<Book> findAll() {
    return bookRepository.findAll();
}
```

* `readOnly = true` erklärt: Hier wird nichts geändert.
* Hibernate spart sich dann das Dirty Checking — die Snapshots der geladenen Entities entfallen, was bei großen Ergebnismengen spürbar Speicher und Zeit spart.
* Zugleich ist es Dokumentation: Die Signatur sagt dem nächsten Leser, dass diese Methode keine Schreibabsicht hat.
* Faustregel: `readOnly = true` an alle Service-Methoden, die nur lesen — schreibende Methoden bekommen das schlichte `@Transactional`.

---

## Wo gehört @Transactional hin?

An die **Service-Schicht** — dort, wo der fachliche Anwendungsfall definiert ist:

* **Nicht an den Controller:** HTTP-Verarbeitung und JSON-Serialisierung haben in einer Datenbanktransaktion nichts verloren.
* **Nicht ans Repository:** Ein Anwendungsfall umfasst oft mehrere Repository-Aufrufe — die Klammer muss um **alle** liegen, sonst committet jeder Aufruf einzeln.
* Die Service-Methode ist genau die richtige Größe: `raisePrices` ist ein fachlicher Vorgang, also eine Transaktion.

Zwei Stolpersteine der Mechanik — Spring setzt die Annotation über einen **Proxy** um (dasselbe Muster wie bei den Repositories):

* Nur **public** Methoden, die **von außen** aufgerufen werden, sind transaktional.
* Ein Aufruf `this.raisePrices(...)` innerhalb derselben Klasse läuft am Proxy vorbei — die Annotation wirkt dann nicht.

Propagation und Isolation — was passiert, wenn Transaktionen aufeinandertreffen — behandelt das Aufbaumodul *13_Data*.

---

# Datenbank und Konfiguration

---

## H2 für die Entwicklung

Womit reden wir eigentlich? Für Entwicklung und Schulung: **H2**, eine In-Memory-Datenbank — die komplette `application.properties` der Demo:

```properties
spring.datasource.url=jdbc:h2:mem:bookstore
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.h2.console.enabled=true
```

* `jdbc:h2:mem:bookstore` — die Datenbank lebt im Speicher der JVM: Start mit leerer Datenbank, Stopp löscht alles. Für Demos ideal, als Persistenz natürlich absurd.
* Mehr braucht es nicht: Liegt nur H2 auf dem Klassenpfad, konfiguriert die AutoConfiguration aus Modul 01 die `DataSource` sogar ganz ohne URL.
* `show-sql=true` schreibt jedes erzeugte Statement ins Log — in der Demo unser wichtigstes Fenster in Hibernates Arbeit.

---

## Die H2-Console

H2 bringt eine Weboberfläche mit — SQL direkt gegen die laufende Anwendung:

```properties
spring.h2.console.enabled=true
```

* Erreichbar unter `http://localhost:8080/h2-console` — als JDBC-URL die URL aus den Properties eintragen: `jdbc:h2:mem:bookstore`.
* Voraussetzung: ein **Web-Starter** auf dem Klassenpfad, denn die Console ist ein Servlet. Unsere Demo läuft bewusst ohne Web-Server — dort bleibt die Property wirkungslos, und wir lesen stattdessen das SQL-Log.
* Nützlich, um Abfragen von Hand zu prüfen: Was steht wirklich in `book`, nachdem der Seed-Runner lief?
* Verbindungsdaten gelten pro JVM: Die Console sieht nur die Datenbank **ihrer** Anwendung.

---

## ddl-auto: die vier Werte

Woher kam eigentlich das Schema? `spring.jpa.hibernate.ddl-auto` steuert, was Hibernate beim Start mit dem Schema macht:

| Wert | Beim Start | Einsatz |
| --- | --- | --- |
| `validate` | Nur prüfen: Passt das Schema zu den Entities? | Produktion |
| `update` | Fehlende Tabellen/Spalten ergänzen, nie löschen | Verlockend — s. nächste Folie |
| `create` | Schema löschen und neu erzeugen | Frühe Entwicklung |
| `create-drop` | Wie `create`, zusätzlich löschen beim Stopp | Demos, Tests |

* Der Boot-Default hängt von der Datenbank ab: **embedded** (H2) → `create-drop`, alles andere → keine Schema-Verwaltung (`none`).
* Deshalb entstand unsere Tabelle "von selbst": H2 plus Default — die Demo setzt `create-drop` nur explizit, um es sichtbar zu machen.

---

## Warum ddl-auto nicht in Produktion gehört

`update` klingt nach dem perfekten Kompromiss — Schema wächst automatisch mit. Genau davor sei gewarnt:

* **Umbenennungen versteht es nicht:** Aus `netPrice` → `price` macht `update` eine **neue Spalte** `price` — die alte bleibt samt Daten verwaist zurück.
* **Es löscht nie, verengt nie, migriert nie Daten:** Typänderungen, `not null` auf gefüllten Tabellen, Umzug von Werten — alles außerhalb seiner Möglichkeiten.
* **Keine Historie:** Welches Schema Version 1.3 hatte, weiß niemand. Rollback eines Deployments? Das Schema rollt nicht mit.
* Und `create`/`create-drop` in Produktion heißt schlicht: **Datenverlust beim Start.**

Der professionelle Weg: **versionierte Migrations-Skripte**, die wie Code im Repository liegen — die Werkzeuge dafür, **Flyway** und **Liquibase**, behandelt das Aufbaumodul *13_Data*.

**Merksatz:** In Produktion `validate` — das Schema ändert nur, wer ein Migrationswerkzeug bedient.

---

## Umstieg auf PostgreSQL

Der Lohn der Abstraktion: Von H2 zu PostgreSQL ist ein **Konfigurationswechsel**, kein Umbau. Im POM den Treiber tauschen:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

In den Properties die Verbindung — typischerweise in einem Profil (`application-prod.properties`, Modul 01):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bookstore
spring.datasource.username=bookstore
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
```

* Entities, Repositories, JPQL, Services: **unverändert** — Hibernate erzeugt jetzt PostgreSQL-SQL.
* `ddl-auto` steht auf `validate` — das Schema kommt ab hier aus Migrationen, nicht aus Hibernate.

---

# Demo

---

## Was wir gleich bauen

`demos/sb-basics-data-jpa-demo` — Startpunkt ist die `-start`-Variante: nur `DataDemoApplication` und `application.properties`, **kein Web-Server**. Alles Sichtbare passiert im SQL-Log. Sieben Schritte:

| Schritt | Baustein | Aha-Moment |
| --- | --- | --- |
| 1 | Entity `Book` | `create table book` — Tabelle aus der Klasse |
| 2 | `Author` + `@ManyToOne`/`@OneToMany` | Fremdschlüssel aus der Objektreferenz |
| 3 | `BookRepository` + `SeedDataRunner` | CRUD ohne eine Zeile Implementierung |
| 4 | Drei Derived Queries | Der Methodenname wird zum SQL |
| 5 | `findByAuthorName` per `@Query` | JPQL navigiert die Beziehung — Hibernate joint |
| 6 | `BookService.raisePrices` | Dirty Checking: UPDATEs ohne `save()` |
| 7 | `raisePricesAndFail` | UPDATEs im Log — und trotzdem Rollback |

* Schritt 7 ist der Lehrmoment des Moduls: Die Statements laufen sichtbar — und die Datenbank bleibt trotzdem unverändert.
* Jede Behauptung ist in `BookRepositoryTest` und `BookServiceTransactionTest` als Test abgesichert.

---

# Übung

---

## Assignment 03

`assignments/sb-basics-data-jpa-assignment` — dieselben Bausteine, wieder die **Kursverwaltung** aus Modul 02: `Course` und `Trainer` statt `Book` und `Author`.

* Gegeben: alle Typen fertig ausgeliefert — `CourseAdminApplication`, `Trainer` (fertige Entity), `Course` (noch **ohne** JPA-Annotationen), `CourseRepository`, Testdaten.
* Ausgangszustand: Die Tests sind rot, weil der Kontext nicht hochkommt — `Not a managed type: Course`.

Drei Aufgaben, jede durch Tests abgesichert:

1. `Course` als Entity mappen: `@Entity`, `@Id` mit `@GeneratedValue`, `@Column(unique = true)` auf `code`, `@ManyToOne` auf `trainer`
2. `findByFeeRange` mit `@Query` und `@Param` implementieren — der Name ist bewusst **nicht** ableitbar
3. `findSummaries` mit `@Query` implementieren

* Der Aha-Moment: Nach Aufgabe 1 funktionieren `findByCode`, `findByTitleContainingIgnoreCase` und `findBySeatsGreaterThan` **ohne eine Zeile Code** — die Namen genügen.
* Bonus: durchschnittliche Netto-Gebühr je Trainer per Aggregat-`@Query`. Musterlösung: `solutions/sb-basics-data-jpa-solution`.

**Ausblick:** Modul 04 testet die Buchhandlung durch alle Schichten — inklusive `@DataJpaTest` gegen genau die Repositories von heute.
