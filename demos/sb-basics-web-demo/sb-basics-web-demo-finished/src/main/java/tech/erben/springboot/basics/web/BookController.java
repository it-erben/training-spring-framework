package tech.erben.springboot.basics.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * REST-Schnittstelle des Buchkatalogs. {@code @RestController} bedeutet:
 * Rueckgabewerte werden direkt in den Response-Body serialisiert (JSON via
 * Jackson), kein View-Rendering. Der Controller uebersetzt nur zwischen
 * HTTP und Fachlogik — die eigentliche Arbeit macht der {@link BookService}.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /** {@code GET /api/books} — 200 mit allen Buechern inklusive Bruttopreis. */
    @GetMapping
    public List<BookResponse> list() {
        return bookService.findAll().stream()
                .map(BookResponse::from)
                .toList();
    }

    /**
     * {@code GET /api/books/{isbn}} — 200 mit dem Buch. Bei unbekannter ISBN
     * wirft der Service eine {@link BookNotFoundException}, die der
     * {@link RestExceptionHandler} in 404 uebersetzt.
     */
    @GetMapping("/{isbn}")
    public BookResponse get(@PathVariable String isbn) {
        return BookResponse.from(bookService.findByIsbn(isbn));
    }

    /**
     * {@code POST /api/books} — 201 mit Location-Header auf die neue
     * Ressource. {@code @Valid} loest die Bean-Validation-Pruefung des
     * {@link BookRequest} aus; schlaegt sie fehl, kommt es gar nicht erst
     * bis in diese Methode (400).
     */
    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        Book book = bookService.create(request.toBook());
        return ResponseEntity
                .created(URI.create("/api/books/" + book.isbn()))
                .body(BookResponse.from(book));
    }

    /**
     * {@code DELETE /api/books/{isbn}} — 204 ohne Body. Bei unbekannter ISBN
     * wieder 404 ueber den Exception-Handler.
     */
    @DeleteMapping("/{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String isbn) {
        bookService.delete(isbn);
    }
}
