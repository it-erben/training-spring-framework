package tech.erben.springboot.basics.core;

import java.math.BigDecimal;

/**
 * Preisberechnung fuer ein Buch. Von diesem Interface existieren zwei
 * Bean-Implementierungen — der Container muss die Mehrdeutigkeit aufloesen
 * ({@code @Primary} bzw. {@code @Qualifier}).
 */
public interface PriceCalculator {

    BigDecimal calculate(Book book);
}
