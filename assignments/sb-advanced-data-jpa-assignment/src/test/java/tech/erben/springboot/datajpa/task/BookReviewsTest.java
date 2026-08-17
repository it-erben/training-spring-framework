package tech.erben.springboot.datajpa.task;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookReviewsTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Aufgabe 2: Reviews werden beim Speichern eines Books kaskadiert")
    void savingBookCascadesReviews() {
        Book book = new Book("Effective Java", "Joshua Bloch", "9780134685991");
        book.setReviews(List.of(
                new Review("Klarer Stil", 5),
                new Review("Viele Praxisbeispiele", 4)
        ));

        Book saved = bookRepository.save(book);

        assertThat(bookRepository.findById(saved.getId()).orElseThrow().getReviews())
                .hasSize(2);
    }

    @Test
    @DisplayName("Aufgabe 2: findWithReviewsByTitle lädt Reviews per EntityGraph")
    void findWithReviewsByTitleLoadsReviewsEagerly() {
        Book book = new Book("Effective Java", "Joshua Bloch", "9780134685991");
        book.setReviews(List.of(new Review("Klarer Stil", 5)));
        bookRepository.save(book);

        List<Book> found = bookRepository.findWithReviewsByTitle("Effective Java");

        assertThat(found)
                .hasSize(1)
                .first()
                .extracting(b -> b.getReviews().size())
                .isEqualTo(1);
    }
}
