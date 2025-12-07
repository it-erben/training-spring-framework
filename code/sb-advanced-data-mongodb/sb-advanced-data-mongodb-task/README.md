# Aufgabe 1 

Um euch mit der Query Language von MongoDB vertraut zu machen, nehmt euch etwas Zeit, um native Queries zu schreiben.
Geht zum Playground auf [https://mongoplayground.net/]() und führt die Query-Beispiele aus [https://www.mongodb.com/docs/manual/tutorial/query-documents/]() aus.

# Aufgabe 2

Schreibt eine Reihe von Unit-Tests für das `PersonRepository`. Ihr solltet jedes Property einmal abtesten und verschiedene Stile der Abfrage verwenden: `MongoTemplate`, Query By Example und Queries, die aus Methodennamen im Repository generiert werden.

# Aufgabe 3
Erstellt eine Modellklasse "Phonenumber" mit zwei Integers, einen für den Country Code und einen für die Telefonnummer. Erweitert dann das `Person`-Modell, dass es eine _Liste_ von `Phonenumber` enthält. 
Schreibt einen Test, der eine `Person` mit drei `Phonenumber` speichert und lädt.
Was denkt ihr: Wie wird das Modell in MongoDB abgespeichert?

