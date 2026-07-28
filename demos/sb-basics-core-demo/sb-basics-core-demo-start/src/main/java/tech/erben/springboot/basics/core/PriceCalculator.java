package tech.erben.springboot.basics.core;

import java.math.BigDecimal;

/**
 * Preisberechnung fuer ein Buch. Von diesem Interface entstehen im Modul
 * zwei Bean-Implementierungen — die Mehrdeutigkeit loesen wir live auf.
 */
public interface PriceCalculator {

    BigDecimal calculate(Book book);
}
