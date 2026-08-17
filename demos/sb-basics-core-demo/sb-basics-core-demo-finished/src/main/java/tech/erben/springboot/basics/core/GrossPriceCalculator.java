package tech.erben.springboot.basics.core;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Schlägt 19 % Mehrwertsteuer auf den Nettopreis auf. {@code @Primary}
 * macht diese Bean zum Standard, wenn ein {@link PriceCalculator} ohne
 * {@code @Qualifier} injiziert wird.
 */
@Component("grossPriceCalculator")
@Primary
public class GrossPriceCalculator implements PriceCalculator {

    private static final BigDecimal VAT_FACTOR = new BigDecimal("1.19");

    @Override
    public BigDecimal calculate(Book book) {
        return book.netPrice()
                .multiply(VAT_FACTOR)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
