package tech.erben.springboot.basics.core.task;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fachlogik der Kursverwaltung. Der einzige Konstruktor zeigt beide Wege,
 * die Mehrdeutigkeit zwischen den zwei {@link FeeCalculator}-Beans
 * aufzuloesen, nebeneinander:
 *
 * <ul>
 *   <li>{@code defaultCalculator} traegt keinen Qualifier — durch
 *       {@code @Primary} injiziert der Container den
 *       {@link GrossFeeCalculator}.</li>
 *   <li>{@code netCalculator} waehlt per {@code @Qualifier} explizit den
 *       {@link NetFeeCalculator}.</li>
 * </ul>
 */
@Service
public class CourseService {

    private final CourseCatalog courseCatalog;
    private final FeeCalculator defaultCalculator;
    private final FeeCalculator netCalculator;

    public CourseService(CourseCatalog courseCatalog,
                         FeeCalculator defaultCalculator,
                         @Qualifier("netFeeCalculator") FeeCalculator netCalculator) {
        this.courseCatalog = courseCatalog;
        this.defaultCalculator = defaultCalculator;
        this.netCalculator = netCalculator;
    }

    public List<Course> findAll() {
        return courseCatalog.findAll();
    }

    /** Bruttogebuehr — berechnet vom {@code @Primary}-Kalkulator. */
    public BigDecimal feeFor(Course course) {
        return defaultCalculator.calculate(course);
    }

    /** Nettogebuehr — berechnet vom explizit gewaehlten Kalkulator. */
    public BigDecimal netFeeFor(Course course) {
        return netCalculator.calculate(course);
    }
}
