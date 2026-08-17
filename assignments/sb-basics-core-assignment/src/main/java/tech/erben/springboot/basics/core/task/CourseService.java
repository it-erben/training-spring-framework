package tech.erben.springboot.basics.core.task;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fachlogik der Kursverwaltung. Die Methodenrümpfe sind fertig, aber die
 * Klasse ist noch keine Bean, und die drei Abhängigkeiten werden nie
 * zugewiesen. Beides löst die Konstruktor-Injection:
 *
 * <ul>
 *   <li>{@code defaultCalculator} soll der Standard-Kalkulator sein
 *       (das regelt {@code @Primary} am {@link GrossFeeCalculator}).</li>
 *   <li>{@code netCalculator} soll gezielt der
 *       {@link NetFeeCalculator} sein (das regelt {@code @Qualifier}
 *       am Konstruktor-Parameter).</li>
 * </ul>
 */
// TODO Aufgabe 1: als Spring-Bean deklarieren (Stereotyp-Annotation)
public class CourseService {

    // TODO Aufgabe 1-3: Konstruktor schreiben, der alle drei Abhängigkeiten
    //  entgegennimmt und zuweist. Für netCalculator den passenden
    //  @Qualifier am Parameter setzen.
    private CourseCatalog courseCatalog;
    private FeeCalculator defaultCalculator;
    private FeeCalculator netCalculator;

    public List<Course> findAll() {
        return courseCatalog.findAll();
    }

    /** Bruttogebühr, berechnet vom Standard-Kalkulator. */
    public BigDecimal feeFor(Course course) {
        return defaultCalculator.calculate(course);
    }

    /** Nettogebühr, berechnet vom explizit gewählten Kalkulator. */
    public BigDecimal netFeeFor(Course course) {
        return netCalculator.calculate(course);
    }
}
