# Übung 03: Data und JPA — die Kursverwaltung bekommt eine Datenbank

Die Kursverwaltung speichert ihre Kurse bisher nur im Speicher — jetzt bekommt sie eine echte Datenbank. Anders als in den bisherigen Übungen schreibt ihr hier fast keinen Code: Alle Klassen und sogar alle Repository-Methoden sind bereits deklariert. Was fehlt, sind die Annotationen, die JPA und Spring Data sagen, was sie mit diesen Deklarationen anfangen sollen — eine Entity-Abbildung und zwei JPQL-Queries.

## Lernziele

- Eine Klasse mit `@Entity`, `@Id`/`@GeneratedValue`, `@Column` und `@ManyToOne` auf eine Tabelle abbilden
- Verstehen, dass Derived Queries allein aus dem Methodennamen entstehen — und wo diese Ableitung an ihre Grenze stößt
- Eine JPQL-Query mit `@Query` und benannten Parametern (`@Param`) schreiben
- Mit `select new` in eine Record-Projektion statt in komplette Entities selektieren

## Voraussetzungen

- Java 21, Maven 3.8+
- Inhalte aus Modul *03 Data und JPA* (Folien und Demo `demos/sb-basics-data-jpa-demo`)

## Was liegt bereit?

Alle Klassen liegen in `src/main/java` unter `tech.erben.springboot.basics.data.task`:

| Klasse | Zustand |
| --- | --- |
| `CourseAdminApplication` | Fertig — Einstiegspunkt mit `@SpringBootApplication` |
| `Trainer` | Fertig — bereits gemappte Entity, eure Vorlage für Aufgabe 1 |
| `Course` | Felder, Konstruktoren und Getter fertig — **es fehlt jede JPA-Annotation** |
| `CourseSummary` | Fertig — das Record für die Projektion aus Aufgabe 3 |
| `CourseRepository` | Alle fünf Methoden deklariert — **den letzten beiden fehlt ihre `@Query`** |
| `CourseRepositoryTest` | Die fünf Tests, die den Fortschritt messen |
| `src/test/resources/test-courses.sql` | Testdaten: zwei Trainer, vier Kurse |

Das Besondere an dieser Übung: Drei der fünf Repository-Methoden (`findByCode`, `findByTitleContainingIgnoreCase`, `findBySeatsGreaterThan`) müsst ihr **gar nicht anfassen**. Sobald `Course` eine Entity ist, leitet Spring Data ihre Queries komplett aus den Methodennamen ab — ohne eine Zeile Implementierung. Die anderen beiden Methoden sind bewusst so benannt, dass diese Ableitung scheitert: `Course` hat weder ein Feld `feeRange` noch eines namens `summaries`. Hier müsst ihr die Query selbst mitgeben.

Der Startzustand ist **absichtlich rot**, und zwar komplett: `mvn test -DskipAssignmentTests=false` meldet fünf Fehler, alle mit derselben Ursache — der Spring-Kontext kommt nicht hoch, weil `Course` keine Entity ist (`IllegalArgumentException: Not a managed type`). Anders als in Übung 02 werden die Tests hier **nicht einzeln grün**: Solange auch nur eine der drei Aufgaben offen ist, scheitert schon der Aufbau des Kontexts, und damit fallen alle fünf Tests gemeinsam durch. Euer Fortschritt zeigt sich stattdessen an der Fehlermeldung. Nach Aufgabe 1 lautet sie nicht mehr `Not a managed type`, sondern `QueryCreationException: … No property 'feeRange' found for type 'Course'` — Spring Data versucht beim Start, `findByFeeRange` aus dem Namen abzuleiten, und findet das Feld nicht. Das ist kein Fehler in eurer Lösung von Aufgabe 1, sondern der erwartete Zwischenstand. Nach Aufgabe 2 wandert dieselbe Meldung zu `findSummaries` weiter, und erst nach Aufgabe 3 startet der Kontext — dann laufen alle fünf Tests, und die drei zu den abgeleiteten Methoden sind sofort grün.

## Aufgaben

1. **`Course` als Entity mappen** (Tests: *Ohne Zutun*)
   - Markiert die Klasse mit `@Entity` — `Trainer` zeigt, wie es aussehen soll.
   - Macht `id` mit `@Id` und `@GeneratedValue` zum Primärschlüssel.
   - Sichert `code` mit `@Column(unique = true)` gegen Duplikate ab.
   - Mappt `trainer` mit `@ManyToOne` — daraus entsteht die Fremdschlüssel-Spalte `trainer_id`.
   - Lasst die Tests laufen und lest die Fehlermeldung: Sie sollte jetzt `feeRange` statt `Not a managed type` nennen — das ist Aufgabe 2.
2. **`findByFeeRange` eine Query mitgeben** (Test: *Aufgabe 2*)
   - Annotiert die Methode mit `@Query` und schreibt JPQL, das alle Kurse liefert, deren `netFee` zwischen `min` und `max` liegt (inklusive der Grenzen — `between` tut genau das).
   - Bindet die beiden Argumente mit `@Param` an die benannten Parameter der Query.
   - Sobald eine `@Query` an der Methode steht, ist der Name nur noch ein Name — Spring Data versucht keine Ableitung mehr.
3. **`findSummaries` als Konstruktor-Projektion schreiben** (Test: *Aufgabe 3*)
   - Annotiert die Methode mit `@Query` und nutzt `select new tech.erben.springboot.basics.data.task.CourseSummary(…)`, um pro Kurs direkt ein Record aus Kurscode und Trainername zu bauen.
   - Der Trainername liegt nicht in `Course`: Navigiert in JPQL über die Beziehung (`c.trainer.name`) — den Join erledigt Hibernate.

Zwischenstand jederzeit prüfen:

```bash
mvn test -DskipAssignmentTests=false
```

## Bonusaufgabe (optional)

- Schreibt eine Aggregat-Query: die durchschnittliche Netto-Gebühr je Trainer. Legt euch dafür ein eigenes Record an (z. B. `TrainerAverageFee` mit Trainername und Durchschnitt) und befüllt es per `select new` mit `avg(…)` und `group by`. Achtung: `avg` liefert in JPQL ein `Double`, kein `BigDecimal`. Schreibt einen eigenen Test dazu — die vier Kurse aus `test-courses.sql` geben das Ergebnis vor.

## Erfolgskriterien

- [ ] `mvn test -DskipAssignmentTests=false` läuft grün — alle fünf Tests in `CourseRepositoryTest` bestehen
- [ ] `Trainer`, `CourseSummary` und die drei abgeleiteten Repository-Methoden sind unverändert geblieben
- [ ] Im `CourseRepository` steht keine einzige Methodenimplementierung — nur Deklarationen und Annotationen

## Lösung

Siehe `solutions/sb-basics-data-jpa-solution`.
