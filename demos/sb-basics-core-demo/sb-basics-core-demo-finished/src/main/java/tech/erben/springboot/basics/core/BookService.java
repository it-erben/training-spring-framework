package tech.erben.springboot.basics.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fachlogik der Buchhandlung. Zeigt zwei Wege, die Mehrdeutigkeit zwischen
 * den beiden {@link PriceCalculator}-Beans aufzuloesen:
 *
 * <ul>
 *   <li>Der Konstruktor verlangt einen {@link PriceCalculator} ohne
 *       Qualifier — durch {@code @Primary} injiziert der Container den
 *       {@link GrossPriceCalculator}.</li>
 *   <li>Die Setter-Methode waehlt per {@code @Qualifier} explizit den
 *       {@link NetPriceCalculator}.</li>
 * </ul>
 */
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final PriceCalculator defaultCalculator;

    private PriceCalculator netCalculator;

    public BookService(BookRepository bookRepository, PriceCalculator defaultCalculator) {
        this.bookRepository = bookRepository;
        this.defaultCalculator = defaultCalculator;
    }

    @Autowired
    public void setNetCalculator(@Qualifier("netPriceCalculator") PriceCalculator netCalculator) {
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
