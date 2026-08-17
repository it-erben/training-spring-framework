/**
 * Modul 02 — Web und REST: Ausgangszustand für das Live-Coding.
 *
 * <p>Die Fachlogik ({@link tech.erben.springboot.basics.web.Book},
 * {@link tech.erben.springboot.basics.web.BookService},
 * {@link tech.erben.springboot.basics.web.BookNotFoundException}) ist
 * fertig. Die komplette Web-Schicht fehlt noch — die vier Typen unten
 * entstehen live, die Schrittnummern verweisen auf die README des Moduls.
 */
// TODO: Modul 02 — Schritt 1: BookController anlegen (@RestController, @RequestMapping("/api/books"), zuerst GET-Liste)
// TODO: Modul 02 — Schritt 2: BookResponse-Record anlegen (netPrice plus berechneter grossPrice, statische Factory from(Book))
// TODO: Modul 02 — Schritt 4: RestExceptionHandler anlegen (@RestControllerAdvice, BookNotFoundException → 404, später Validierungsfehler → 400)
// TODO: Modul 02 — Schritt 5: BookRequest-Record anlegen (Bean Validation: @NotBlank isbn, @NotBlank @Size(max = 200) title, @NotNull @Positive netPrice)
package tech.erben.springboot.basics.web;
