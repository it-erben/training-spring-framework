/**
 * Uebung 02 — Web und REST: Ausgangszustand.
 *
 * <p>Die Fachlogik ({@link tech.erben.springboot.basics.web.task.Course},
 * {@link tech.erben.springboot.basics.web.task.CourseService},
 * {@link tech.erben.springboot.basics.web.task.CourseNotFoundException})
 * ist fertig. Die komplette Web-Schicht fehlt noch — die vier Typen unten
 * baut ihr selbst, die Aufgabennummern verweisen auf die README der Uebung.
 */
// TODO Aufgabe 1: CourseResponse-Record anlegen (code, title, seats, netFee plus berechneter grossFee mit 19 % Mehrwertsteuer; statische Factory from(Course))
// TODO Aufgabe 1: CourseController anlegen (@RestController, @RequestMapping("/api/courses"), GET-Liste ueber den CourseService)
// TODO Aufgabe 2: im CourseController GET /{code} ergaenzen (@PathVariable)
// TODO Aufgabe 2: RestExceptionHandler anlegen (@RestControllerAdvice, CourseNotFoundException → 404)
// TODO Aufgabe 3: CourseRequest-Record anlegen (code, title, seats, netFee; Methode toCourse())
// TODO Aufgabe 3: im CourseController POST ergaenzen (201 mit Location-Header via ResponseEntity.created)
// TODO Aufgabe 3: im CourseController DELETE /{code} ergaenzen (204; unbekannter Code laeuft in euren 404-Handler)
// TODO Aufgabe 4: CourseRequest mit Bean Validation versehen (@NotBlank code, @NotBlank @Size(max = 200) title, @Positive seats, @NotNull @Positive netFee) und den Controller-Parameter mit @Valid markieren
// TODO Aufgabe 4: RestExceptionHandler um MethodArgumentNotValidException → 400 erweitern
// TODO Bonusaufgabe: PUT /{code} ergaenzen (200 mit dem aktualisierten Kurs, unbekannter Code → 404 via CourseService.update)
package tech.erben.springboot.basics.web.task;
