package tech.erben.springboot.basics.core.task;

import java.math.BigDecimal;

/**
 * Gebuehrenberechnung fuer einen Kurs. Von diesem Interface existieren
 * zwei Implementierungen — sobald beide Beans sind, muss der Container
 * die Mehrdeutigkeit aufloesen ({@code @Primary} bzw. {@code @Qualifier}).
 */
public interface FeeCalculator {

    BigDecimal calculate(Course course);
}
