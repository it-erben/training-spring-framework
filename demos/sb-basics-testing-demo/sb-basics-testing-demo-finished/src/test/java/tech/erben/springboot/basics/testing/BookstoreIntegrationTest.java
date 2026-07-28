package tech.erben.springboot.basics.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrationstest: {@code RANDOM_PORT} startet die komplette Anwendung
 * mit echtem Tomcat und In-Memory-H2, das {@link TestRestTemplate} schickt
 * echte HTTP-Requests dagegen. Hier ist nichts gemockt — dafuer ist dieser
 * Test die mit Abstand langsamste der drei Testarten. Jeder Test benutzt
 * eine eigene ISBN, damit die Tests unabhaengig von ihrer Reihenfolge
 * bleiben (der Kontext und damit die Datenbank wird zwischen den Tests
 * nicht neu gestartet).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class BookstoreIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("POST legt ein Buch an, GET liest es wieder aus der Datenbank")
    void createdBookCanBeReadBack() {
        Book book = new Book();
        book.setIsbn("978-0-13-475759-9");
        book.setTitle("Refactoring");
        book.setNetPrice(new BigDecimal("46.60"));

        ResponseEntity<Book> created =
                restTemplate.postForEntity("/api/books", book, Book.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getHeaders().getLocation())
                .isEqualTo(URI.create("/api/books/978-0-13-475759-9"));

        ResponseEntity<Book> fetched =
                restTemplate.getForEntity("/api/books/978-0-13-475759-9", Book.class);

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody()).isNotNull();
        assertThat(fetched.getBody().getTitle()).isEqualTo("Refactoring");
        // Die Id vergibt erst die Datenbank — sie beweist, dass das Buch
        // wirklich gespeichert wurde und nicht nur der Request zurueckkam.
        assertThat(fetched.getBody().getId()).isNotNull();
    }

    @Test
    @DisplayName("Rabattregel Ende-zu-Ende: 5 Exemplare zu 100.00 kosten 450.00")
    void totalAppliesDiscountEndToEnd() {
        Book book = new Book();
        book.setIsbn("978-0-13-468599-1");
        book.setTitle("Effective Java");
        book.setNetPrice(new BigDecimal("100.00"));
        ResponseEntity<Book> created =
                restTemplate.postForEntity("/api/books", book, Book.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        BigDecimal total = restTemplate.getForObject(
                "/api/books/978-0-13-468599-1/total?quantity=5", BigDecimal.class);

        assertThat(total).isEqualByComparingTo(new BigDecimal("450.00"));
    }
}
