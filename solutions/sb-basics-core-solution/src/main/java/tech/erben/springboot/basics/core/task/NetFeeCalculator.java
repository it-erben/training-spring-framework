package tech.erben.springboot.basics.core.task;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Gibt die Nettogebuehr unveraendert zurueck. Der Bean-Name
 * {@code netFeeCalculator} dient als Ziel fuer {@code @Qualifier}.
 */
@Component("netFeeCalculator")
public class NetFeeCalculator implements FeeCalculator {

    @Override
    public BigDecimal calculate(Course course) {
        return course.netFee();
    }
}
