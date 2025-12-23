package tech.erben.springboot.webdemo.service;

import org.springframework.stereotype.Service;
import tech.erben.springboot.webdemo.api.BookRequest;
import tech.erben.springboot.webdemo.model.Book;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BookService {

    private final Map<Long, Book> catalog = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public BookService() {
        seed();
    }

    public List<Book> findAll() {
        return catalog
            .values()
            .stream()
            .sorted(Comparator.comparing(Book::getId))
            .toList();
    }

    public Book findById(Long id) {
        return Optional
            .ofNullable(catalog.get(id))
            .orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book create(BookRequest request) {
        long id = sequence.incrementAndGet();
        Book book = new Book(
            id,
            request.getTitle(),
            request.getAuthor(),
            request.getCategory(),
            request.getPrice(),
            request.getPublicationYear(),
            request.getOriginCountry(),
            Instant.now()
        );
        catalog.put(id, book);
        return book;
    }

    public Book update(Long id, BookRequest request) {
        Book existing = findById(id);
        if (request.getTitle() != null) {
            existing.setTitle(request.getTitle());
        }
        if (request.getAuthor() != null) {
            existing.setAuthor(request.getAuthor());
        }
        if (request.getCategory() != null) {
            existing.setCategory(request.getCategory());
        }
        if (request.getPrice() != null) {
            existing.setPrice(request.getPrice());
        }
        if (request.getPublicationYear() != null) {
            existing.setPublicationYear(request.getPublicationYear());
        }
        if (request.getOriginCountry() != null) {
            existing.setOriginCountry(request.getOriginCountry());
        }
        return existing;
    }

    public void delete(Long id) {
        if (catalog.remove(id) == null) {
            throw new BookNotFoundException(id);
        }
    }

    private void seed() {
        add(
            new Book(
                null,
                "HTTP/2 in Action",
                "Barry Pollard",
                "technology",
                new BigDecimal("42.50"),
                2019,
                "GB",
                Instant.now()
            )
        );
        add(
            new Book(
                null,
                "Domain-Driven Design",
                "Eric Evans",
                "business",
                new BigDecimal("55.00"),
                2003,
                "US",
                Instant.now()
            )
        );
    }

    private void add(Book book) {
        long id = sequence.incrementAndGet();
        book.setId(id);
        if (book.getCreatedAt() == null) {
            book.setCreatedAt(Instant.now());
        }
        catalog.put(id, book);
    }
}
