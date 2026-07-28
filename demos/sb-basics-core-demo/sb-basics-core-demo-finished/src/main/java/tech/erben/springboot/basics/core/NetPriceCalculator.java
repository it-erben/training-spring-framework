package tech.erben.springboot.basics.core;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Gibt den Nettopreis unveraendert zurueck. Der Bean-Name
 * {@code netPriceCalculator} dient als Ziel fuer {@code @Qualifier}.
 */
@Component("netPriceCalculator")
public class NetPriceCalculator implements PriceCalculator {

    @Override
    public BigDecimal calculate(Book book) {
        return book.netPrice();
    }
}
