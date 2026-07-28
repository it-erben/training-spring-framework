# Demo: Data und JPA — Entities, Repositories und Transaktionen

Diese Demo baut die Persistenzschicht der Buchhandlung: JPA-Entities fuer
Buecher und Autoren, ein Spring-Data-Repository mit Derived Queries und
JPQL, dazu ein Service mit `@Transactional` — inklusive der Rollback-Demo,
die zeigt, dass bei einer RuntimeException die komplette Transaktion
zurueckgerollt wird. Als Datenbank dient eine In-Memory-H2; das Schema
erzeugt Hibernate beim Start aus den Entities (`ddl-auto=create-drop`).
Das taugt nur fuer Demos und Tests: In Produktion kaeme das Schema aus
versionierten Migrationen (Flyway oder Liquibase, siehe Aufbaumodul
*13_Data*), und `ddl-auto` stuende auf `validate`.

Anders als in Modul 02 gibt es keinen Web-Server: Die Anwendung startet,
der `SeedDataRunner` laeuft einmal durch, danach faehrt der Kontext wieder
herunter. Alles Sichtbare passiert im Log — `spring.jpa.show-sql=true`
zeigt jedes SQL-Statement, das Hibernate erzeugt.

## Datenmodell und Repository

Ein `Book` (`isbn`, `title`, `netPrice`) gehoert per `@ManyToOne` zu einem
`Author` (`name`); die Gegenseite ist ein
`@OneToMany(mappedBy = "author")`. Das `BookRepository` kommt ohne eine
Zeile Implementierung aus:

| Methode | Mechanik |
| --- | --- |
| `findByIsbn(String)` | Derived Query: `where isbn = ?` |
| `findByTitleContainingIgnoreCase(String)` | Derived Query: `where upper(title) like upper('%…%')` |
| `findByNetPriceLessThan(BigDecimal)` | Derived Query: `where net_price < ?` |
| `findByAuthorName(String)` | JPQL per `@Query` — Join ueber die Beziehung |

## Varianten

| Modul | Inhalt |
| --- | --- |
| `sb-basics-data-jpa-demo-start` | Nur `DataDemoApplication` und `application.properties` — Entities, Repository, Service und Seed-Runner entstehen live. `TODO: Modul 03`-Marken in `package-info.java` verweisen mit Schrittnummern auf den Ablauf unten. |
| `sb-basics-data-jpa-demo-finished` | Vollstaendige Persistenzschicht inklusive Tests. |

## Ablauf der Live-Demo

Ausgangspunkt ist die `-start`-Variante. Jeder Zwischenzustand startet —
nach jedem Schritt lohnt ein Neustart, um die neue Log-Ausgabe zu zeigen.
Vorab einmal ohne Aenderung starten: Die Anwendung faehrt hoch und gleich
wieder herunter, und im Log taucht kein einziges `create table` auf — es
gibt noch keine Entity.

1. **Erste Entity:** `Book` anlegen — `@Entity`, `@Id @GeneratedValue`
   auf `Long id`, dazu die Felder `isbn`, `title` und
   `BigDecimal netPrice`. Anders als die Records aus Modul 02 braucht eine
   Entity einen (geschuetzten) parameterlosen Konstruktor und
   veraenderbare Felder — Hibernate erzeugt Instanzen per Reflection und
   schreibt die Spaltenwerte direkt hinein. Dazu ein oeffentlicher
   Konstruktor fuer die drei Fachfelder, Getter und `setNetPrice`. Nach
   dem Neustart zeigt das Log, dass die Tabelle aus der Klasse entsteht:

   ```text
   Hibernate: create sequence book_seq start with 1 increment by 50
   Hibernate: create table book (net_price numeric(38,2), id bigint not null, ...)
   ```

2. **Beziehung:** `Author` anlegen (`@Entity`, `Long id`, `String name`,
   `@OneToMany(mappedBy = "author") List<Book> books`) und in `Book` die
   Gegenrichtung ergaenzen — Feld
   `@ManyToOne(cascade = CascadeType.PERSIST) Author author`, dazu
   Konstruktor-Parameter und Getter. `mappedBy` sagt Hibernate, dass die
   Fremdschluessel-Spalte bereits durch `Book.author` definiert ist; das
   Cascade speichert einen noch nicht gespeicherten Autor beim Speichern
   des Buchs mit (das zahlt sich in Schritt 3 aus). Der Neustart zeigt
   `create table author` und den Fremdschluessel:

   ```text
   Hibernate: alter table if exists book add constraint ... foreign key (author_id) references author
   ```

3. **Repository und Seed-Daten:** `BookRepository` als leeres Interface
   `extends JpaRepository<Book, Long>` anlegen — kein einziges
   Implementierungsdetail, Spring Data erzeugt zur Laufzeit ein
   Proxy-Objekt mit fertigem CRUD (`save`, `findAll`, `count`, ...).
   Dazu `SeedDataRunner` als `CommandLineRunner`: zwei Autoren
   (Joshua Bloch, Martin Fowler), fuenf Buecher per `saveAll`, danach
   `System.out.println(">>> Seed: " + bookRepository.count() + " ...")`.
   Im Log stehen jetzt zwei `insert into author` und fuenf
   `insert into book` — die Autoren speichert das Cascade mit — und:

   ```text
   >>> Seed: 5 Buecher angelegt
   ```

4. **Derived Queries:** Drei Methoden im Repository ergaenzen — Spring
   Data leitet die Abfragen aus den Methodennamen ab:

   ```java
   Optional<Book> findByIsbn(String isbn);
   List<Book> findByTitleContainingIgnoreCase(String fragment);
   List<Book> findByNetPriceLessThan(BigDecimal limit);
   ```

   Im Runner alle drei aufrufen und ausgeben. Der Neustart zeigt zu jeder
   Ausgabe das erzeugte SQL (`where isbn = ?`, `like upper(?)`,
   `net_price < ?`):

   ```text
   >>> findByIsbn("978-0-13-468599-1"): Effective Java
   >>> findByTitleContainingIgnoreCase("java"): [Effective Java, Java Puzzlers]
   >>> findByNetPriceLessThan(40.00): [Java Puzzlers, UML Distilled]
   ```

5. **JPQL:** Wo der Methodenname unlesbar wuerde, hilft `@Query`:

   ```java
   @Query("select b from Book b where b.author.name = :name")
   List<Book> findByAuthorName(@Param("name") String name);
   ```

   `b.author.name` navigiert ueber die Beziehung — im SQL-Log wird daraus
   ein `join author` auf die Fremdschluessel-Spalte. Der Aufruf im Runner
   liefert:

   ```text
   >>> findByAuthorName("Martin Fowler"): [Refactoring, Patterns of Enterprise Application Architecture, UML Distilled]
   ```

6. **Service mit Transaktion:** `BookService` anlegen —
   Konstruktor-Injection des Repositories und eine Methode
   `@Transactional void raisePrices(BigDecimal factor)`, die alle Buecher
   laedt und jeden Preis mit dem Faktor multipliziert (kaufmaennisch
   gerundet). Auffaellig: nirgendwo ein `save()` — innerhalb der
   Transaktion sind die geladenen Entities "managed", Hibernate erkennt
   die Aenderung per Dirty Checking und schreibt beim Commit die
   UPDATE-Statements selbst. Im Runner den Service injizieren und die
   Preise vorher/nachher ausgeben:

   ```text
   >>> Preise vor raisePrices(1.10): [44.99, 35.00, 46.60, 54.95, 39.95]
   Hibernate: update book set author_id=?,isbn=?,net_price=?,title=? where id=?
   >>> Preise nach raisePrices(1.10): [49.49, 38.50, 51.26, 60.45, 43.95]
   ```

7. **Rollback-Demo:** Zweite Service-Methode — dieselbe Preisaenderung,
   aber danach fliegt absichtlich eine Exception:

   ```java
   @Transactional
   public void raisePricesAndFail(BigDecimal factor) {
       applyFactor(factor);
       bookRepository.flush();
       throw new IllegalStateException(
               "Absichtlicher Fehler nach der Preisaenderung — die Transaktion rollt zurueck");
   }
   ```

   Das `flush()` zwingt Hibernate, die UPDATE-Statements sofort
   auszufuehren — sie erscheinen im Log, und trotzdem steht nach dem
   Rollback wieder der alte Preis in der Datenbank. Im Runner den Aufruf
   in `try/catch` packen und danach die Preise erneut ausgeben:

   ```text
   >>> IllegalStateException gefangen: Absichtlicher Fehler nach der Preisaenderung — die Transaktion rollt zurueck
   >>> Preise nach raisePricesAndFail(2.00) — unveraendert dank Rollback: [49.49, 38.50, 51.26, 60.45, 43.95]
   ```

Damit ist der Stand der `-finished`-Variante erreicht (dort ist `run()`
lediglich noch in die drei Methoden `seed()`, `demonstrateQueries()` und
`demonstrateTransactions()` gegliedert).

## Starten

```bash
cd sb-basics-data-jpa-demo-start
mvn spring-boot:run
```

```bash
cd sb-basics-data-jpa-demo-finished
mvn spring-boot:run
```

Alternativ als Fat-JAR: `mvn package` und dann
`java -jar target/sb-basics-data-jpa-demo-finished-1.0.0-SNAPSHOT.jar`.

## Tests

Nur die `-finished`-Variante hat Tests (in der `-start`-Variante fehlt
die Persistenzschicht, die sie pruefen wuerden):

```bash
cd sb-basics-data-jpa-demo-finished
mvn test
```

`BookRepositoryTest` laeuft als `@DataJpaTest` gegen definierte Testdaten
aus `test-books.sql` (zwei Autoren, drei Buecher), unabhaengig vom
Seed-Runner; `BookServiceTransactionTest` laeuft als `@SpringBootTest`
bewusst ohne `@Transactional` am Test, um Commit und Rollback der
Service-Transaktion von aussen zu beobachten. Jede Verhaltensbehauptung
ist durch einen Test abgesichert:

| Behauptung | Test |
| --- | --- |
| `findByIsbn` liefert das Buch zur ISBN | `findsByIsbn` |
| `findByIsbn` liefert ein leeres `Optional` bei unbekannter ISBN | `findsNothingForUnknownIsbn` |
| Titelsuche findet Fragmente unabhaengig von Gross-/Kleinschreibung | `findsByTitleFragment` |
| Preisgrenze filtert auf genau die Buecher unterhalb des Limits | `findsBelowPriceLimit` |
| JPQL-Query findet Buecher ueber den Autorennamen (Join) | `findsByAuthorName` |
| Cascade PERSIST speichert einen neuen Autor beim Speichern des Buchs mit | `cascadesAuthorPersist` |
| `raisePrices` hebt alle Preise per Dirty Checking an (kein `save()`) und committet | `raisesAllPrices` |
| `raisePricesAndFail` fuehrt die UPDATEs tatsaechlich aus (`flush()`), wirft `IllegalStateException`, und die Aenderung wird zurueckgerollt | `rollsBackOnFailure` (zaehlt die Entity-Updates per Hibernate-Statistik — ohne echte UPDATEs vor dem Rollback wird er rot) |
