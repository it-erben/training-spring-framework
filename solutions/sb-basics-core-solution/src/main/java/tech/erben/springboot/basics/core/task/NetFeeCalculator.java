package tech.erben.springboot.basics.core.task;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Gibt die Nettogebühr unverändert zurück. Der Bean-Name
 * {@code netFeeCalculator} dient als Ziel für {@code @Qualifier}.
 */
@Component("netFeeCalculator")
public class NetFeeCalculator implements FeeCalculator {

    @Override
    public BigDecimal calculate(Course course) {
        return course.netFee();
    }
}
