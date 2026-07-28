package tech.erben.springboot.basics.data.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Keine einzige Implementierung: Die ersten drei Methoden leitet Spring
 * Data komplett aus dem Namen ab, die letzten beiden bekommen ihre JPQL
 * per {@link Query} mit — die Loesungen zu Aufgabe 2 und 3.
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

    /** Derived Query: {@code where code = ?}. */
    Optional<Course> findByCode(String code);

    /** Derived Query: {@code where upper(title) like upper('%fragment%')}. */
    List<Course> findByTitleContainingIgnoreCase(String fragment);

    /** Derived Query: {@code where seats > ?}. */
    List<Course> findBySeatsGreaterThan(int minimum);

    /**
     * Loesung zu Aufgabe 2: Sobald eine {@code @Query} dransteht, ist der
     * Methodenname nur noch ein Name — Spring Data versucht keine Ableitung
     * mehr. {@code @Param} bindet die Argumente an die benannten Parameter.
     */
    @Query("select c from Course c where c.netFee between :min and :max")
    List<Course> findByFeeRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);

    /**
     * Loesung zu Aufgabe 3: Konstruktor-Projektion — {@code select new}
     * ruft fuer jede Ergebniszeile den Konstruktor von
     * {@link CourseSummary} auf; {@code c.trainer.name} navigiert die
     * Beziehung, Hibernate joint dafuer auf {@code trainer}.
     */
    @Query("select new tech.erben.springboot.basics.data.task.CourseSummary(c.code, c.trainer.name) from Course c")
    List<CourseSummary> findSummaries();
}
