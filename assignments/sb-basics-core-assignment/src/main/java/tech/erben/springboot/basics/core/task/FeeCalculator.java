package tech.erben.springboot.basics.core.task;

import java.math.BigDecimal;

/**
 * Gebührenberechnung für einen Kurs. Von diesem Interface existieren
 * zwei Implementierungen, sobald beide Beans sind, muss der Container
 * die Mehrdeutigkeit auflösen ({@code @Primary} bzw. {@code @Qualifier}).
 */
public interface FeeCalculator {

    BigDecimal calculate(Course course);
}
