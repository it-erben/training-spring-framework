package tech.erben.springboot.basics.web;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Ausgabe-DTO für die REST-Schnittstelle. Zusätzlich zum Domänenmodell
 * enthält es den berechneten Bruttopreis (19 % Mehrwertsteuer) — ein
 * typischer Grund, nach außen ein eigenes DTO statt der internen Klasse
 * zu verwenden.
 */
public record BookResponse(String isbn, String title,
                           BigDecimal netPrice, BigDecimal grossPrice) {

    private static final BigDecimal VAT_FACTOR = new BigDecimal("1.19");

    public static BookResponse from(Book book) {
        BigDecimal grossPrice = book.netPrice()
                .multiply(VAT_FACTOR)
                .setScale(2, RoundingMode.HALF_UP);
        return new BookResponse(book.isbn(), book.title(),
                book.netPrice(), grossPrice);
    }
}
