package tech.erben.springboot.basics.data;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Bewusst KEIN {@code @Transactional} am Test: Der Test soll den echten
 * Commit bzw. Rollback der Service-Transaktion von außen beobachten,
 * nicht selbst Teil der Transaktion sein.
 *
 * <p>Die Hibernate-Statistiken sind aktiviert, damit der Rollback-Test
 * nachweisen kann, dass die UPDATE-Statements tatsächlich ausgeführt
 * wurden, bevor sie zurückgerollt werden. Ohne diesen Zähler wäre der
 * Test wertlos: Fehlt {@code @Transactional} am Service, wird nie ein
 * UPDATE abgesetzt (die Entities sind detached) — die Preise wären
 * ebenfalls "unverändert", aber aus dem falschen Grund.
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class BookServiceTransactionTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    void resetBooks() {
        bookRepository.deleteAll();
        bookRepository.save(new Book("978-0-13-468599-1", "Effective Java",
                new BigDecimal("44.99"), null));
        bookRepository.save(new Book("978-0-321-33678-1", "Java Puzzlers",
                new BigDecimal("35.00"), null));
    }

    @Test
    @DisplayName("raisePrices hebt alle Preise um den Faktor an und committet")
    void raisesAllPrices() {
        bookService.raisePrices(new BigDecimal("1.10"));

        assertThat(priceOf("978-0-13-468599-1")).isEqualByComparingTo("49.49");
        assertThat(priceOf("978-0-321-33678-1")).isEqualByComparingTo("38.50");
    }

    @Test
    @DisplayName("Cascade PERSIST speichert einen neuen Autor beim Speichern des Buchs mit")
    void cascadesAuthorPersist() {
        bookRepository.save(new Book("978-0-321-14653-3", "Test Driven Development",
                new BigDecimal("30.00"), new Author("Kent Beck")));

        assertThat(bookRepository.findByAuthorName("Kent Beck"))
                .extracting(Book::getTitle)
                .containsExactly("Test Driven Development");
    }

    @Test
    @DisplayName("raisePricesAndFail führt die UPDATEs aus, wirft IllegalStateException und rollt zurück")
    void rollsBackOnFailure() {
        long updatesBefore = statistics().getEntityUpdateCount();

        assertThatThrownBy(() -> bookService.raisePricesAndFail(new BigDecimal("1.10")))
                .isInstanceOf(IllegalStateException.class);

        // Erst der Nachweis, dass wirklich geschrieben wurde (das flush()
        // hat beide UPDATEs ausgeführt) — sonst wären die "unveränderten"
        // Preise kein Beleg für einen Rollback:
        assertThat(statistics().getEntityUpdateCount() - updatesBefore)
                .as("Anzahl der vor dem Rollback ausgeführten Entity-Updates")
                .isEqualTo(2);

        assertThat(priceOf("978-0-13-468599-1")).isEqualByComparingTo("44.99");
        assertThat(priceOf("978-0-321-33678-1")).isEqualByComparingTo("35.00");
    }

    private Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    private BigDecimal priceOf(String isbn) {
        return bookRepository.findByIsbn(isbn).orElseThrow().getNetPrice();
    }
}
