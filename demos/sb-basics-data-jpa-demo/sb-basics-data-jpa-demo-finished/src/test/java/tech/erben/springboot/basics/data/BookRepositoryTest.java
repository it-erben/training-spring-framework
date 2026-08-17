package tech.erben.springboot.basics.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/test-books.sql")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("findByIsbn findet ein vorhandenes Buch")
    void findsByIsbn() {
        assertThat(bookRepository.findByIsbn("978-0-13-468599-1"))
                .hasValueSatisfying(book -> assertThat(book.getTitle()).isEqualTo("Effective Java"));
    }

    @Test
    @DisplayName("findByIsbn liefert ein leeres Optional bei unbekannter ISBN")
    void findsNothingForUnknownIsbn() {
        assertThat(bookRepository.findByIsbn("000-0-00-000000-0")).isEmpty();
    }

    @Test
    @DisplayName("Derived Query sucht Titelfragmente unabhängig von Groß-/Kleinschreibung")
    void findsByTitleFragment() {
        assertThat(bookRepository.findByTitleContainingIgnoreCase("java"))
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Effective Java", "Java Puzzlers");
    }

    @Test
    @DisplayName("Derived Query filtert nach Preisgrenze")
    void findsBelowPriceLimit() {
        assertThat(bookRepository.findByNetPriceLessThan(new BigDecimal("40.00")))
                .extracting(Book::getTitle)
                .containsExactly("Java Puzzlers");
    }

    @Test
    @DisplayName("JPQL-Query findet Bücher über den Autorennamen")
    void findsByAuthorName() {
        assertThat(bookRepository.findByAuthorName("Joshua Bloch"))
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Effective Java", "Java Puzzlers");
    }
}
