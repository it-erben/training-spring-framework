package tech.erben.springboot.basics.testing;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Fachlogik der Buchhandlung. Das interessanteste Stück ist die
 * Rabattregel in {@link #totalFor(String, int)}: ab fünf Exemplaren gibt
 * es zehn Prozent Nachlass. Genau solche Regeln mit Grenzfällen (vier
 * Exemplare? fünf?) sind der klassische Fall für schnelle Unit-Tests
 * ohne Spring-Kontext.
 */
@Service
public class BookService {

    /** Ab dieser Stückzahl greift der Mengenrabatt. */
    private static final int DISCOUNT_THRESHOLD = 5;

    /** Zehn Prozent Nachlass — es bleiben 90 Prozent des Preises. */
    private static final BigDecimal DISCOUNT_FACTOR = new BigDecimal("0.90");

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));
    }

    public Book create(Book book) {
        return bookRepository.save(book);
    }

    /**
     * Gesamtpreis für {@code quantity} Exemplare des Buchs zur ISBN.
     * Ab {@link #DISCOUNT_THRESHOLD} Exemplaren wird der Mengenrabatt
     * abgezogen, das Ergebnis ist kaufmännisch auf zwei Nachkommastellen
     * gerundet.
     */
    public BigDecimal totalFor(String isbn, int quantity) {
        Book book = findByIsbn(isbn);
        BigDecimal total = book.getNetPrice()
                .multiply(BigDecimal.valueOf(quantity));
        if (quantity >= DISCOUNT_THRESHOLD) {
            total = total.multiply(DISCOUNT_FACTOR);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
