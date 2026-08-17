# Übung: Library Management mit Advanced JPA Features

Ziel: Erweitere die Bibliotheks-Verwaltung um Beziehungen, Auditing, Projections
und Modifying Queries.

## Aufgabe 1: Embedded Type & Auditing

1. Erstelle eine Klasse `PublisherInfo` mit Feldern `String name` und
   `String location`. Annotiere sie mit `@Embeddable`.
2. Erweitere die `Book`-Entity:
   * Füge ein Feld `PublisherInfo publisherInfo` hinzu (`@Embedded`).
   * Füge Auditing-Felder hinzu: `createdAt` und `updatedAt` (`LocalDateTime`)
   und annotiere sie mit den richtigen Annotationen (`@CreatedDate`,
   `@LastModifiedDate`)
   * Annotiere die Entity mit `@EntityListeners(AuditingEntityListener.class)`.
   `@EnableJpaAuditing` steht bereits an `LibraryApplication`.

## Aufgabe 2: One-to-Many Beziehung & EntityGraph

1. Erstelle eine Entity `Review` mit `String comment`, `int rating` (1-5, nur
   dokumentarisch — keine Bean-Validation-Abhängigkeit im Modul) und einer
   `id` (`@Id`/`@GeneratedValue`, analog zu `Book`).
2. Füge der `Book`-Entity eine Liste von Reviews hinzu (`@OneToMany`,
   unidirektional genügt, z. B. mit `@JoinColumn`). Kaskadiert Persist/Merge
   (`CascadeType.ALL`) — sonst scheitert das Speichern eines Books mit
   Reviews an einer transienten Entity-Referenz.
3. Erstelle im `BookRepository` die Methode
   `List<Book> findWithReviewsByTitle(String title)`, die mittels
   `@EntityGraph(attributePaths = "reviews")` die Reviews sofort mitlädt
   (Eager Fetching).

## Aufgabe 3: Projections & DTOs

1. **Interface Projection**: Wir wollen manchmal nur den Buchtitel und die ISBN
   sehen. Erstelle ein Interface `BookIdentity` mit `getTitle()` und
   `getIsbn()`. Ergänze im Repository die Methode
   `List<BookIdentity> findAllBy()` (Spring-Data-Konvention für "alle
   Datensätze, aber projiziert" — kein Filterkriterium im Methodennamen).
2. **DTO mit JPQL**: Erstelle eine Klasse `BookAuthorDTO` (keine Record-Klasse;
   Felder `title`, `authorName` mit Gettern). Schreibe im Repository die
   Methode `List<BookAuthorDTO> findAllBookAuthorDTOs()` mit einer
   JPQL-Konstruktor-Query (`SELECT new ...`), die dieses DTO direkt befüllt.
   Die Query braucht den vollqualifizierten Klassennamen des DTOs
   (`tech.erben.springboot.datajpa.task.BookAuthorDTO`).

## Aufgabe 4: Runner Implementation

Erweitere den `CommandLineRunner`:

* Lege Bücher mit PublisherInfo und Reviews an.
* Zeige, dass `createdAt` gesetzt wurde.
* Suche ein Buch mit Reviews und gib die Anzahl der Reviews aus (EntityGraph
  Test).
* Lade nur die `BookIdentity` Projection.
* Lade die `BookAuthorDTO`s.
