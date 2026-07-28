package tech.erben.springboot.basics.testing;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;

/**
 * REST-Schnittstelle der Buchhandlung — der Vertrag aus Modul 02, auf das
 * Noetigste reduziert. Der Controller uebersetzt nur zwischen HTTP und
 * Fachlogik; genau deshalb laesst er sich im Slice-Test isoliert pruefen,
 * indem der {@link BookService} durch ein Mock ersetzt wird.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * {@code GET /api/books/{isbn}} — 200 mit dem Buch. Bei unbekannter
     * ISBN wirft der Service eine {@link BookNotFoundException}, die der
     * {@link RestExceptionHandler} in 404 uebersetzt.
     */
    @GetMapping("/{isbn}")
    public Book get(@PathVariable String isbn) {
        return bookService.findByIsbn(isbn);
    }

    /**
     * {@code GET /api/books/{isbn}/total?quantity=n} — der Gesamtpreis
     * fuer n Exemplare inklusive Mengenrabatt, als nackte Zahl im Body.
     */
    @GetMapping("/{isbn}/total")
    public BigDecimal total(@PathVariable String isbn, @RequestParam int quantity) {
        return bookService.totalFor(isbn, quantity);
    }

    /**
     * {@code POST /api/books} — 201 mit Location-Header auf die neue
     * Ressource. {@code @Valid} loest die Bean-Validation-Pruefung der
     * Annotationen am {@link Book}-Modell aus.
     */
    @PostMapping
    public ResponseEntity<Book> create(@Valid @RequestBody Book book) {
        Book saved = bookService.create(book);
        return ResponseEntity
                .created(URI.create("/api/books/" + saved.getIsbn()))
                .body(saved);
    }
}
