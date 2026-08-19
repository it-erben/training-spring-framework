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
 *
 * <p>Damit die drei abgeleiteten Methoden schon vor Aufgabe 2 und 3 testbar
 * sind, tragen die letzten beiden vorerst einen {@code default}-Rumpf:
 * Methoden mit Rumpf sind für Spring Data keine Query-Methoden, es versucht
 * gar nicht erst, sie aus dem Namen abzuleiten, und der Kontext startet.
 * Der Rumpf gewinnt aber immer — eine {@code @Query} daneben bliebe
 * wirkungslos. Deshalb gehört er in beiden Aufgaben gelöscht.
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

    /** Derived Query: {@code where code = ?}. Funktioniert ab Aufgabe 1 ohne weiteres Zutun. */
    Optional<Course> findByCode(String code);

    /** Derived Query: {@code where upper(title) like upper('%fragment%')} - funktioniert ab Aufgabe 1. */
    List<Course> findByTitleContainingIgnoreCase(String fragment);

    /** Derived Query: {@code where seats > ?} - funktioniert ab Aufgabe 1. */
    List<Course> findBySeatsGreaterThan(int minimum);

    /**
     * Es gibt kein Feld {@code feeRange}. Ohne Rumpf und ohne {@code @Query}
     * versucht Spring Data die Ableitung aus dem Namen und der Kontext-Start
     * bricht ab. Erst eine {@code @Query} entbindet den Namen von der
     * Ableitungsregel.
     */
    // TODO: Modul 03, Aufgabe 2: default-Rumpf löschen, @Query mit JPQL ergänzen (between) und min/max per @Param an die benannten Parameter binden
    default List<Course> findByFeeRange(BigDecimal min, BigDecimal max) {
        throw new UnsupportedOperationException(
                "Aufgabe 2: default-Rumpf löschen und die Methode mit @Query deklarieren");
    }

    /**
     * Auch hier scheitert die Ableitung: {@code summaries} ist kein Feld
     * von {@link Course}. Die Query baut per {@code select new} direkt
     * {@link CourseSummary}-Instanzen — Konstruktor-Projektion.
     */
    // TODO: Modul 03, Aufgabe 3: default-Rumpf löschen, @Query mit select new tech.erben.springboot.basics.data.task.CourseSummary(...) ergänzen
    default List<CourseSummary> findSummaries() {
        throw new UnsupportedOperationException(
                "Aufgabe 3: default-Rumpf löschen und die Methode mit @Query deklarieren");
    }
}
