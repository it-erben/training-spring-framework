package tech.erben.springboot.basics.core.task;

import java.math.BigDecimal;

/**
 * Gibt die Nettogebuehr unveraendert zurueck. Der Bean-Name
 * {@code netFeeCalculator} dient spaeter als Ziel fuer {@code @Qualifier}.
 */
// TODO Aufgabe 3: als Spring-Bean mit dem Namen "netFeeCalculator" deklarieren
public class NetFeeCalculator implements FeeCalculator {

    @Override
    public BigDecimal calculate(Course course) {
        return course.netFee();
    }
}
