package tech.erben.springboot.basics.core;

import java.math.BigDecimal;

/**
 * Gibt den Nettopreis unverändert zurück.
 */
// TODO: Modul 00 — Schritt 2: mit @Component("netPriceCalculator") als Bean deklarieren
public class NetPriceCalculator implements PriceCalculator {

    @Override
    public BigDecimal calculate(Book book) {
        return book.netPrice();
    }
}
