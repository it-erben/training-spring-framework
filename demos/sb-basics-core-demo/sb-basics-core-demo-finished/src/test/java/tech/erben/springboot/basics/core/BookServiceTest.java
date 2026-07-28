package tech.erben.springboot.basics.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("Der Katalog enthaelt die drei Beispielbuecher")
    void catalogContainsThreeBooks() {
        assertThat(bookService.findAll()).hasSize(3);
    }

    @Test
    @DisplayName("Ohne Qualifier greift der @Primary GrossPriceCalculator")
    void defaultCalculatorAddsVat() {
        Book book = new Book("978-3-16-148410-0", "Spring im Einsatz",
                new BigDecimal("100.00"));

        assertThat(bookService.priceFor(book))
                .isEqualByComparingTo(new BigDecimal("119.00"));
    }

    @Test
    @DisplayName("Mit Qualifier greift der NetPriceCalculator")
    void qualifiedCalculatorReturnsNetPrice() {
        Book book = new Book("978-3-16-148410-0", "Spring im Einsatz",
                new BigDecimal("100.00"));

        assertThat(bookService.netPriceFor(book))
                .isEqualByComparingTo(new BigDecimal("100.00"));
    }
}
