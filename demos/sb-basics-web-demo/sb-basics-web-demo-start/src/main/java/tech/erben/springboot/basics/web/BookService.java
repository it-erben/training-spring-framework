package tech.erben.springboot.basics.web;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-Memory-Verwaltung des Buchkatalogs. Die Fachlogik weiß nichts von
 * HTTP, JSON oder Statuscodes — sie arbeitet nur mit {@link Book} und
 * wirft bei unbekannter ISBN eine {@link BookNotFoundException}.
 *
 * <p>Diese Klasse ist fertig — in der Live-Demo entsteht die
 * REST-Schnittstelle darüber (siehe README).
 */
@Service
public class BookService {

    /**
     * Eine synchronisierte {@link LinkedHashMap} reicht für die Demo: Sie
     * behält die Einfügereihenfolge, damit die Ausgabe von Aufruf zu
     * Aufruf stabil bleibt.
     */
    private final Map<String, Book> books =
            Collections.synchronizedMap(new LinkedHashMap<>());

    public BookService() {
        create(new Book("978-3-8362-9049-8",
                "Spring Boot 3 und Spring Framework 6", new BigDecimal("49.90")));
        create(new Book("978-3-8362-8745-2",
                "Java ist auch eine Insel", new BigDecimal("49.90")));
    }

    // TODO: Modul 02 — Schritt 1: vom BookController für GET /api/books aufrufen
    public List<Book> findAll() {
        return List.copyOf(books.values());
    }

    // TODO: Modul 02 — Schritt 3: vom BookController für GET /api/books/{isbn} aufrufen
    public Book findByIsbn(String isbn) {
        Book book = books.get(isbn);
        if (book == null) {
            throw new BookNotFoundException(isbn);
        }
        return book;
    }

    // TODO: Modul 02 — Schritt 5: vom BookController für POST /api/books aufrufen (Eingabe kommt als validierter BookRequest)
    public Book create(Book book) {
        books.put(book.isbn(), book);
        return book;
    }

    // TODO: Modul 02 — Schritt 6: vom BookController für DELETE /api/books/{isbn} aufrufen
    public void delete(String isbn) {
        if (books.remove(isbn) == null) {
            throw new BookNotFoundException(isbn);
        }
    }
}
