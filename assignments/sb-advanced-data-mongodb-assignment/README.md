# Aufgabe 1

Um euch mit der Query Language von MongoDB vertraut zu machen, nehmt euch etwas Zeit, um native Queries zu schreiben.
Geht zum Playground auf [mongoplayground.net](https://mongoplayground.net/) und führt die Query-Beispiele aus der [MongoDB-Doku](https://www.mongodb.com/docs/manual/tutorial/query-documents/) aus.

# Aufgabe 2

Schreibt eine Reihe von Unit-Tests für das `PersonRepository`. Ihr solltet jedes Property einmal abtesten und verschiedene Stile der Abfrage verwenden: `MongoTemplate`, Query By Example und Queries, die aus Methodennamen im Repository generiert werden.

`PersonRepository` bringt bisher nur `findPersonsByBirthdayAfter` als Methodennamen-Query mit. Für `firstname` und `lastname` ergänzt ihr die Methoden `findPersonsByFirstname` und `findPersonsByLastname` selbst im Repository-Interface.

Für die `MongoTemplate`-Query: `MongoTemplate` steht im `@DataMongoTest`-Kontext bereits als Bean bereit, einfach per `@Autowired` injizieren (`org.springframework.data.mongodb.core.MongoTemplate`, Query-Bau mit `org.springframework.data.mongodb.core.query.Query` und `Criteria`).

# Aufgabe 3

Erstellt eine Modellklasse `Phonenumber` (Record) mit zwei Integers, einen für den Country Code und einen für die Telefonnummer. Erweitert dann das `Person`-Modell, dass es eine _Liste_ von `Phonenumber` enthält.

`Person` ist ein Record — ein zusätzliches Feld ändert die Konstruktor-Arität. Passt die Hilfsmethoden in `PersonRepositoryTest`, die `Person` instanziieren, entsprechend an.

Schreibt einen Test, der eine `Person` mit drei `Phonenumber` speichert und lädt.
Was denkt ihr: Wie wird das Modell in MongoDB abgespeichert?
