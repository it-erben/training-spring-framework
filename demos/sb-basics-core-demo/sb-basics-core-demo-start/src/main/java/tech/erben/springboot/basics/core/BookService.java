package tech.erben.springboot.basics.core;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fachlogik der Buchhandlung. Die Klasse ist fertig geschrieben, aber noch
 * keine Bean — der Container instanziiert sie erst, wenn sie deklariert ist.
 */
// TODO: Modul 00 — Schritt 2: mit @Service als Bean deklarieren
public class BookService {

    private final BookRepository bookRepository;
    private final PriceCalculator defaultCalculator;
    private final PriceCalculator netCalculator;

    public BookService(BookRepository bookRepository,
                       PriceCalculator defaultCalculator,
                       // TODO: Modul 00 — Schritt 4: @Qualifier("netPriceCalculator") an diesen Parameter — der unqualifizierte davor behält die @Primary-Bean
                       PriceCalculator netCalculator) {
        this.bookRepository = bookRepository;
        this.defaultCalculator = defaultCalculator;
        this.netCalculator = netCalculator;
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    /** Bruttopreis — berechnet vom Standard-Kalkulator. */
    public BigDecimal priceFor(Book book) {
        return defaultCalculator.calculate(book);
    }

    /** Nettopreis — berechnet vom explizit gewählten Kalkulator. */
    public BigDecimal netPriceFor(Book book) {
        return netCalculator.calculate(book);
    }
}
