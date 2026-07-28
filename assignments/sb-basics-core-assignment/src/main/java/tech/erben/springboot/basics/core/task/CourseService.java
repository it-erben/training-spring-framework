package tech.erben.springboot.basics.core.task;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fachlogik der Kursverwaltung. Die Methodenruempfe sind fertig — aber die
 * Klasse ist noch keine Bean, und die drei Abhaengigkeiten werden nie
 * zugewiesen. Beides loest die Konstruktor-Injection:
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

    // TODO Aufgabe 1-3: Konstruktor schreiben, der alle drei Abhaengigkeiten
    //  entgegennimmt und zuweist. Fuer netCalculator den passenden
    //  @Qualifier am Parameter setzen.
    private CourseCatalog courseCatalog;
    private FeeCalculator defaultCalculator;
    private FeeCalculator netCalculator;

    public List<Course> findAll() {
        return courseCatalog.findAll();
    }

    /** Bruttogebuehr — berechnet vom Standard-Kalkulator. */
    public BigDecimal feeFor(Course course) {
        return defaultCalculator.calculate(course);
    }

    /** Nettogebuehr — berechnet vom explizit gewaehlten Kalkulator. */
    public BigDecimal netFeeFor(Course course) {
        return netCalculator.calculate(course);
    }
}
