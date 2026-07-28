package tech.erben.springboot.basics.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Schlaegt 19 % Mehrwertsteuer auf den Nettopreis auf.
 */
// TODO: Modul 00 — als Spring-Bean deklarieren
public class GrossPriceCalculator implements PriceCalculator {

    private static final BigDecimal VAT_FACTOR = new BigDecimal("1.19");

    @Override
    public BigDecimal calculate(Book book) {
        return book.netPrice()
                .multiply(VAT_FACTOR)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
