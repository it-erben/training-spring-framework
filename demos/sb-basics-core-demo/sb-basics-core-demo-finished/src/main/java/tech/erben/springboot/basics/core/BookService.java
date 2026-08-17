package tech.erben.springboot.basics.core;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fachlogik der Buchhandlung. Eine einzige Konstruktor-Signatur zeigt beide
 * Wege, die Mehrdeutigkeit zwischen den zwei {@link PriceCalculator}-Beans
 * aufzulösen, nebeneinander:
 *
 * <ul>
 *   <li>{@code defaultCalculator} trägt keinen Qualifier — durch
 *       {@code @Primary} injiziert der Container den
 *       {@link GrossPriceCalculator}.</li>
 *   <li>{@code netCalculator} wählt per {@code @Qualifier} explizit den
 *       {@link NetPriceCalculator}.</li>
 * </ul>
 */
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final PriceCalculator defaultCalculator;
    private final PriceCalculator netCalculator;

    public BookService(BookRepository bookRepository,
                       PriceCalculator defaultCalculator,
                       @Qualifier("netPriceCalculator") PriceCalculator netCalculator) {
        this.bookRepository = bookRepository;
        this.defaultCalculator = defaultCalculator;
        this.netCalculator = netCalculator;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    /** Bruttopreis — berechnet vom {@code @Primary}-Kalkulator. */
    public BigDecimal priceFor(Book book) {
        return defaultCalculator.calculate(book);
    }

    /** Nettopreis — berechnet vom explizit qualifizierten Kalkulator. */
    public BigDecimal netPriceFor(Book book) {
        return netCalculator.calculate(book);
    }
}
