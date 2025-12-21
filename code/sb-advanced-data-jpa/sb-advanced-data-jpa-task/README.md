# Übung: Library Management mit Advanced JPA Features

Ziel: Erweitere die Bibliotheks-Verwaltung um Beziehungen, Auditing, Projections und Modifying Queries.

## Aufgabe 1: Embedded Type & Auditing

1. Erstelle eine Klasse `PublisherInfo` mit Feldern `String name` und `String location`. Annotiere sie mit `@Embeddable`.
2. Erweitere die `Book`-Entity:
    * Füge ein Feld `PublisherInfo publisherInfo` hinzu (`@Embedded`).
    * Füge Auditing-Felder hinzu: `createdAt` und `updatedAt` (`LocalDateTime`).
    * Vergiss nicht `@EntityListeners(AuditingEntityListener.class)` an der Entity und `@EnableJpaAuditing` an der Applikation!

## Aufgabe 2: One-to-Many Beziehung & EntityGraph

1. Erstelle eine Entity `Review` mit `String comment`, `int rating` (1-5) und einer `id`.
2. Füge der `Book`-Entity eine Liste von Reviews hinzu (`@OneToMany`).
3. Erstelle im `BookRepository` eine Methode `findWithReviewsByTitle(String title)`, die mittels `@EntityGraph` die Reviews sofort mitlädt (Eager Fetching).

## Aufgabe 3: Projections & DTOs

1. **Interface Projection**: Wir wollen manchmal nur den Buchtitel und die ISBN sehen. Erstelle ein Interface `BookIdentity` mit entsprechenden Getter-Methoden. Füge eine Query-Methode im Repository hinzu.
2. **DTO mit JPQL**: Erstelle eine Klasse `BookAuthorDTO` (Felder: `title`, `authorName`). Schreibe eine JPQL-Query im Repository, die dieses DTO direkt befüllt (`SELECT new ...`).

## Aufgabe 4: Modifying Query

1. Schreibe eine Methode `updatePublisherName(String oldName, String newName)` im Repository.
2. Nutze `@Modifying` und `@Query` mit einem UPDATE-Statement.
3. Hinweis: Damit das im Test/Runner funktioniert, benötigst du eine aktive Transaktion (z.B. `@Transactional` am Test/Service).

## Aufgabe 5: Runner Implementation

Erweitere den `CommandLineRunner`:

* Lege Bücher mit PublisherInfo und Reviews an.
* Zeige, dass `createdAt` gesetzt wurde.
* Suche ein Buch mit Reviews und gib die Anzahl der Reviews aus (EntityGraph Test).
* Lade nur die `BookIdentity` Projection.
* Lade die `BookAuthorDTO`s.
* (Optional) Führe das Update aus.
