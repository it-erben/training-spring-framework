package tech.erben.springboot.basics.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Schlaegt 19 % Mehrwertsteuer auf den Nettopreis auf. Hier fehlen zwei
 * Annotationen: die Bean-Deklaration und der Vorrang bei Mehrdeutigkeit.
 */
// TODO: Modul 00 — Schritt 2: mit @Component("grossPriceCalculator") als Bean deklarieren
// TODO: Modul 00 — Schritt 2: sofort auch @Primary — sonst bricht der Start ab ("expected single matching bean but found 2")
public class GrossPriceCalculator implements PriceCalculator {

    private static final BigDecimal VAT_FACTOR = new BigDecimal("1.19");

    @Override
    public BigDecimal calculate(Book book) {
        return book.netPrice()
                .multiply(VAT_FACTOR)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
