package tech.erben.springboot.basics.core.task;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Schlägt 19 % Mehrwertsteuer auf die Nettogebühr auf. {@code @Primary}
 * macht diese Bean zum Standard, wenn ein {@link FeeCalculator} ohne
 * {@code @Qualifier} injiziert wird.
 */
@Component("grossFeeCalculator")
@Primary
public class GrossFeeCalculator implements FeeCalculator {

    private static final BigDecimal VAT_FACTOR = new BigDecimal("1.19");

    @Override
    public BigDecimal calculate(Course course) {
        return course.netFee()
                .multiply(VAT_FACTOR)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
