# Übung 02: Web und REST — die Kursverwaltung bekommt eine API

Die Kursverwaltung aus Übung 00 bekommt eine REST-Schnittstelle: Die Fachlogik (`Course`, `CourseService`) ist fertig verdrahtet, aber nach außen ist die Anwendung stumm — jede Anfrage an `/api/courses` läuft ins Leere. Ihr baut die komplette Web-Schicht selbst: Controller, DTOs, Fehlerbehandlung und Validierung — dieselben Bausteine wie in der Buchhandlungs-Demo, nur mit eurer eigenen Domäne.

## Lernziele

- Einen `@RestController` mit `@GetMapping`, `@PostMapping` und `@DeleteMapping` schreiben
- Die Schnittstelle mit eigenen Request- und Response-DTOs (Records) vom Domänenmodell entkoppeln
- Fachliche Ausnahmen per `@RestControllerAdvice` in die richtigen Statuscodes übersetzen
- Eingaben mit Bean Validation (`@Valid`, `@NotBlank`, `@Positive`) prüfen und `400` liefern

## Voraussetzungen

- Java 21, Maven 3.8+
- Inhalte aus Modul *02 Web und REST* (Folien und Demo `demos/sb-basics-web-demo`)

## Was liegt bereit?

Alle Klassen liegen in `src/main/java` unter `tech.erben.springboot.basics.web.task`:

| Klasse | Zustand |
| --- | --- |
| `CourseAdminApplication` | Fertig — Einstiegspunkt, startet den eingebetteten Tomcat |
| `Course` | Fertig — das interne Domänenmodell als Record |
| `CourseService` | Fertig — In-Memory-Verwaltung, wirft `CourseNotFoundException` |
| `CourseNotFoundException` | Fertig — die fachliche Ausnahme für unbekannte Kurscodes |
| `CourseController` | **Fehlt komplett** — baut ihr in Aufgabe 1–3 |
| `CourseResponse`, `CourseRequest` | **Fehlen komplett** — baut ihr in Aufgabe 1 und 3 |
| `RestExceptionHandler` | **Fehlt komplett** — baut ihr in Aufgabe 2 und 4 |
| `CourseControllerTest` | Die fünf Tests, die den Fortschritt messen |

Die `TODO`-Marken in `package-info.java` listen jeden Handgriff auf. Der Startzustand ist **absichtlich rot**: `mvn test -DskipAssignmentTests=false` schlägt fehl, weil ohne Controller jede Anfrage mit `404` beantwortet wird.

Anders als in Übung 00 werden die Tests hier **einzeln grün**, sobald die jeweilige Aufgabe gelöst ist — der Anwendungskontext startet von Anfang an, es fehlen ja keine Beans. Ein Detail zum Test von *Aufgabe 2*: Er gibt sich nicht mit dem Status zufrieden, sondern prüft auch den JSON-Körper der Fehlerantwort. Die `404`, die Spring für eine schlicht nicht existierende Route liefert, reicht ihm deshalb nicht — grün wird er erst, wenn euer `RestExceptionHandler` die Antwort wirklich erzeugt. Baut ihr `GET /{code}` vor dem Handler, bricht der Test mit der unbehandelten `CourseNotFoundException` ab (im laufenden Server wäre das ein `500` beim Client).

## Aufgaben

1. **Kursliste mit Bruttogebühr ausliefern** (Test: *Aufgabe 1*)
   - Legt den Record `CourseResponse` an: `code`, `title`, `seats`, `netFee` plus die berechnete Bruttogebühr `grossFee` (19 % Mehrwertsteuer, kaufmännisch auf 2 Nachkommastellen gerundet). Eine statische Factory `from(Course)` hält die Umrechnung an einer Stelle.
   - Legt den `CourseController` an (`@RestController`, `@RequestMapping("/api/courses")`) und liefert unter `GET /api/courses` alle Kurse als `CourseResponse`-Liste.
2. **Einzelnen Kurs holen — und `404` statt `500`** (Test: *Aufgabe 2*)
   - Ergänzt `GET /api/courses/{code}` mit `@PathVariable`. Der `CourseService` wirft bei unbekanntem Code eine `CourseNotFoundException` — probiert aus, was ohne weitere Maßnahme beim Client ankommt.
   - Legt den `RestExceptionHandler` an (`@RestControllerAdvice`) und übersetzt die `CourseNotFoundException` in einen `404`, dessen JSON-Körper die Meldung in einem Feld `error` trägt — genau darauf prüft der Test.
3. **Kurse anlegen und löschen** (Tests: *Aufgabe 3*)
   - Legt den Record `CourseRequest` an (`code`, `title`, `seats`, `netFee`) mit einer Methode `toCourse()`.
   - Ergänzt `POST /api/courses`: Antwort `201` mit `Location`-Header auf die neue Ressource (`ResponseEntity.created(…)`).
   - Ergänzt `DELETE /api/courses/{code}` mit Antwort `204` — bei unbekanntem Code greift automatisch euer Handler aus Aufgabe 2. Der zweite Aufgabe-3-Test prüft genau diese Kette: erst `204`, beim zweiten Löschen `404`.
4. **Eingaben validieren** (Test: *Aufgabe 4*)
   - Versehen den `CourseRequest` mit Bean Validation: `code` und `title` nicht leer, `title` maximal 200 Zeichen, `seats` und `netFee` positiv.
   - Markiert den Request-Parameter im Controller mit `@Valid`.
   - Erweitert den `RestExceptionHandler` um `MethodArgumentNotValidException` → `400` mit einer Map von Feldname auf Fehlermeldung.

Zwischenstand jederzeit prüfen:

```bash
mvn test -DskipAssignmentTests=false
```

## Bonusaufgabe (optional)

- Ergänzt `PUT /api/courses/{code}` zum Aktualisieren eines Kurses: Antwort `200` mit dem aktualisierten Kurs, bei unbekanntem Code `404`. Der `CourseService` bringt dafür bereits eine `update`-Methode mit. Überlegt euch: Was soll gewinnen, wenn der Code im Pfad und der Code im Body voneinander abweichen?

## Erfolgskriterien

- [ ] `mvn test -DskipAssignmentTests=false` läuft grün — alle fünf Tests in `CourseControllerTest` bestehen
- [ ] `Course`, `CourseService` und `CourseNotFoundException` sind unverändert geblieben
- [ ] Das Domänenmodell `Course` taucht in keiner Controller-Signatur auf — nach außen gehen nur `CourseRequest` und `CourseResponse`

## Lösung

Siehe `solutions/sb-basics-web-solution`.
