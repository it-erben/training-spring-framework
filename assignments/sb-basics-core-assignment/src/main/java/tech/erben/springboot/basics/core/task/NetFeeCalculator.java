package tech.erben.springboot.basics.core.task;

import java.math.BigDecimal;

/**
 * Gibt die Nettogebühr unverändert zurück. Der Bean-Name
 * {@code netFeeCalculator} dient später als Ziel für {@code @Qualifier}.
 */
// TODO Aufgabe 3: als Spring-Bean mit dem Namen "netFeeCalculator" deklarieren
public class NetFeeCalculator implements FeeCalculator {

    @Override
    public BigDecimal calculate(Course course) {
        return course.netFee();
    }
}
