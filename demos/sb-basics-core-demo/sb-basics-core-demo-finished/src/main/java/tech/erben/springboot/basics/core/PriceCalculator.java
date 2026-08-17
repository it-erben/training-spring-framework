package tech.erben.springboot.basics.core;

import java.math.BigDecimal;

/**
 * Preisberechnung für ein Buch. Von diesem Interface existieren zwei
 * Bean-Implementierungen — der Container muss die Mehrdeutigkeit auflösen
 * ({@code @Primary} bzw. {@code @Qualifier}).
 */
public interface PriceCalculator {

    BigDecimal calculate(Book book);
}
