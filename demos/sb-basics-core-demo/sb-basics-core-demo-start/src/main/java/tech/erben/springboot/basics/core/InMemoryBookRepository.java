package tech.erben.springboot.basics.core;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * In-Memory-Implementierung mit drei festen Buechern.
 */
// TODO: Modul 00 — Schritt 1: mit @Repository als Bean deklarieren
public class InMemoryBookRepository implements BookRepository {

    private final List<Book> books = List.of(
            new Book("978-3-8362-9049-8", "Spring Boot 3 und Spring Framework 6", new BigDecimal("49.90")),
            new Book("978-3-8362-8745-2", "Java ist auch eine Insel", new BigDecimal("49.90")),
            new Book("978-0-13-468599-1", "Effective Java", new BigDecimal("54.90"))
    );

    @Override
    public List<Book> findAll() {
        return books;
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return books.stream()
                .filter(book -> book.isbn().equals(isbn))
                .findFirst();
    }
}
