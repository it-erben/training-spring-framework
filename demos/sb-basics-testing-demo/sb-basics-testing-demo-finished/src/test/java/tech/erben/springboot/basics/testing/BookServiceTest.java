package tech.erben.springboot.basics.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-Test ohne Spring-Kontext: Die {@link MockitoExtension} erzeugt für
 * jedes {@code @Mock}-Feld ein Mock-Objekt und injiziert es in das
 * {@code @InjectMocks}-Feld — hier läuft kein Kontext hoch und keine
 * Datenbank, deshalb sind diese Tests in Millisekunden fertig. Getestet
 * wird die Rabattregel genau an ihrer Grenze: vier Exemplare ohne, fünf
 * mit Rabatt — nur so fällt auf, wenn aus {@code >=} ein {@code >} wird.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    @DisplayName("Ab fünf Exemplaren gibt es zehn Prozent Rabatt")
    void appliesBulkDiscount() {
        Book book = new Book();
        book.setIsbn("978-0-13-468599-1");
        book.setNetPrice(new BigDecimal("100.00"));
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(book));

        BigDecimal total = bookService.totalFor("978-0-13-468599-1", 5);

        assertThat(total).isEqualByComparingTo(new BigDecimal("450.00"));
    }

    @Test
    @DisplayName("Unter fünf Exemplaren gibt es keinen Rabatt")
    void appliesNoDiscountBelowThreshold() {
        Book book = new Book();
        book.setIsbn("978-0-13-468599-1");
        book.setNetPrice(new BigDecimal("100.00"));
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(book));

        BigDecimal total = bookService.totalFor("978-0-13-468599-1", 4);

        assertThat(total).isEqualByComparingTo(new BigDecimal("400.00"));
        // Das Stubbing oben arbeitet mit anyString() — würde der Service
        // eine falsche ISBN ans Repository durchreichen, fände die
        // Rückgabewert-Prüfung das nicht. Erst dieses verify deckt es
        // auf: genau ein Aufruf, und zwar mit der echten ISBN.
        verify(bookRepository, times(1)).findByIsbn("978-0-13-468599-1");
    }
}
