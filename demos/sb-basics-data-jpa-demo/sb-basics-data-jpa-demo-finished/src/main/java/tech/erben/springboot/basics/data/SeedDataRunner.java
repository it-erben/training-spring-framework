package tech.erben.springboot.basics.data;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Laeuft einmal nach dem Start des Kontexts: legt zwei Autoren und fuenf
 * Buecher an und fuehrt danach die Abfragen und die Transaktions-Demo aus
 * dem Live-Ablauf vor (siehe README). Zwischen den {@code >>>}-Zeilen
 * stehen im Log die SQL-Statements, die Hibernate dafuer erzeugt.
 */
@Component
public class SeedDataRunner implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final BookService bookService;

    public SeedDataRunner(BookRepository bookRepository, BookService bookService) {
        this.bookRepository = bookRepository;
        this.bookService = bookService;
    }

    @Override
    public void run(String... args) {
        seed();
        demonstrateQueries();
        demonstrateTransactions();
    }

    /** Zwei Autoren, fuenf Buecher — die Autoren speichert das Cascade mit. */
    private void seed() {
        Author bloch = new Author("Joshua Bloch");
        Author fowler = new Author("Martin Fowler");
        bookRepository.saveAll(List.of(
                new Book("978-0-13-468599-1", "Effective Java", new BigDecimal("44.99"), bloch),
                new Book("978-0-321-33678-1", "Java Puzzlers", new BigDecimal("35.00"), bloch),
                new Book("978-0-13-475759-9", "Refactoring", new BigDecimal("46.60"), fowler),
                new Book("978-0-321-12742-6", "Patterns of Enterprise Application Architecture",
                        new BigDecimal("54.95"), fowler),
                new Book("978-0-321-19368-1", "UML Distilled", new BigDecimal("39.95"), fowler)));
        System.out.println(">>> Seed: " + bookRepository.count() + " Buecher angelegt");
    }

    private void demonstrateQueries() {
        System.out.println(">>> findByIsbn(\"978-0-13-468599-1\"): "
                + bookRepository.findByIsbn("978-0-13-468599-1")
                        .map(Book::getTitle).orElse("nicht gefunden"));
        System.out.println(">>> findByTitleContainingIgnoreCase(\"java\"): "
                + titles(bookRepository.findByTitleContainingIgnoreCase("java")));
        System.out.println(">>> findByNetPriceLessThan(40.00): "
                + titles(bookRepository.findByNetPriceLessThan(new BigDecimal("40.00"))));
        System.out.println(">>> findByAuthorName(\"Martin Fowler\"): "
                + titles(bookRepository.findByAuthorName("Martin Fowler")));
    }

    private void demonstrateTransactions() {
        System.out.println(">>> Preise vor raisePrices(1.10): " + prices());
        bookService.raisePrices(new BigDecimal("1.10"));
        System.out.println(">>> Preise nach raisePrices(1.10): " + prices());
        try {
            bookService.raisePricesAndFail(new BigDecimal("2.00"));
        } catch (IllegalStateException e) {
            System.out.println(">>> IllegalStateException gefangen: " + e.getMessage());
        }
        System.out.println(">>> Preise nach raisePricesAndFail(2.00) — unveraendert dank Rollback: "
                + prices());
    }

    private List<String> titles(List<Book> books) {
        return books.stream().map(Book::getTitle).toList();
    }

    private List<BigDecimal> prices() {
        return bookRepository.findAll().stream().map(Book::getNetPrice).toList();
    }
}
