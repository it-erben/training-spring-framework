package tech.erben.springboot.basics.core.task;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Schlägt 19 % Mehrwertsteuer auf die Nettogebühr auf. Soll der Standard
 * sein, wenn ein {@link FeeCalculator} ohne {@code @Qualifier} injiziert
 * wird, dafür braucht es neben der Bean-Deklaration noch eine zweite
 * Annotation.
 */
// TODO Aufgabe 2: als Spring-Bean deklarieren und zum Standard-Kalkulator machen
public class GrossFeeCalculator implements FeeCalculator {

    private static final BigDecimal VAT_FACTOR = new BigDecimal("1.19");

    @Override
    public BigDecimal calculate(Course course) {
        return course.netFee()
                .multiply(VAT_FACTOR)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
