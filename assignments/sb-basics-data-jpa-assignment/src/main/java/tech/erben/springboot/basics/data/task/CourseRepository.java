package tech.erben.springboot.basics.data.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Alle fünf Methoden sind bereits deklariert. Geschrieben wird hier keine
 * einzige Implementierung. Die ersten drei leitet Spring Data komplett aus
 * dem Methodennamen ab; sie funktionieren, sobald {@link Course} eine
 * Entity ist (Aufgabe 1). Die letzten beiden kann kein Parser erraten.
 * Ihnen gebt ihr in Aufgabe 2 und 3 eine JPQL-Query mit.
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

    /** Derived Query: {@code where code = ?}. Funktioniert ab Aufgabe 1 ohne weiteres Zutun. */
    Optional<Course> findByCode(String code);

    /** Derived Query: {@code where upper(title) like upper('%fragment%')} - funktioniert ab Aufgabe 1. */
    List<Course> findByTitleContainingIgnoreCase(String fragment);

    /** Derived Query: {@code where seats > ?} - funktioniert ab Aufgabe 1. */
    List<Course> findBySeatsGreaterThan(int minimum);

    /**
     * Es gibt kein Feld {@code feeRange}. Aus diesem Namen kann Spring Data
     * keine Query ableiten, der Kontext-Start bricht ab. Erst eine
     * {@code @Query} entbindet den Namen von der Ableitungsregel.
     */
    // TODO: Modul 03, Aufgabe 2: @Query mit JPQL ergänzen (between) und min/max per @Param an die benannten Parameter binden
    List<Course> findByFeeRange(BigDecimal min, BigDecimal max);

    /**
     * Auch hier scheitert die Ableitung: {@code summaries} ist kein Feld
     * von {@link Course}. Die Query baut per {@code select new} direkt
     * {@link CourseSummary}-Instanzen — Konstruktor-Projektion.
     */
    // TODO: Modul 03, Aufgabe 3: @Query mit select new tech.erben.springboot.basics.data.task.CourseSummary(...) ergänzen
    List<CourseSummary> findSummaries();
}
