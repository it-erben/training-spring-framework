package tech.erben.springboot.basics.data.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Misst den Fortschritt der Uebung. Achtung: Solange der Spring-Kontext
 * nicht hochkommt, fallen alle fuenf Tests gemeinsam durch — Details dazu
 * stehen in der README.
 */
@DataJpaTest
@Sql("/test-courses.sql")
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("Ohne Zutun: findByCode findet einen Kurs ueber den eindeutigen Code")
    void findsByCode() {
        assertThat(courseRepository.findByCode("SB-ADV"))
                .hasValueSatisfying(course -> {
                    assertThat(course.getTitle()).isEqualTo("Spring Boot Advanced");
                    assertThat(course.getTrainer().getName()).isEqualTo("Erika Muster");
                });
    }

    @Test
    @DisplayName("Ohne Zutun: findByTitleContainingIgnoreCase sucht Titelfragmente unabhaengig von Gross-/Kleinschreibung")
    void findsByTitleFragment() {
        assertThat(courseRepository.findByTitleContainingIgnoreCase("SPRING"))
                .extracting(Course::getCode)
                .containsExactlyInAnyOrder("SB-ADV", "SB-BAS");
    }

    @Test
    @DisplayName("Ohne Zutun: findBySeatsGreaterThan filtert nach Mindestplatzzahl")
    void findsBySeats() {
        assertThat(courseRepository.findBySeatsGreaterThan(10))
                .extracting(Course::getCode)
                .containsExactlyInAnyOrder("SB-ADV", "JAVA-MOD");
    }

    @Test
    @DisplayName("Aufgabe 2: findByFeeRange liefert genau die Kurse im Gebuehrenbereich (inklusive Grenzen)")
    void findsByFeeRange() {
        assertThat(courseRepository.findByFeeRange(new BigDecimal("1450.00"), new BigDecimal("1700.00")))
                .extracting(Course::getCode)
                .containsExactlyInAnyOrder("SB-ADV", "SB-BAS");
    }

    @Test
    @DisplayName("Aufgabe 3: findSummaries projiziert jeden Kurs auf Code und Trainernamen")
    void findsSummaries() {
        assertThat(courseRepository.findSummaries())
                .containsExactlyInAnyOrder(
                        new CourseSummary("SB-ADV", "Erika Muster"),
                        new CourseSummary("SB-BAS", "Erika Muster"),
                        new CourseSummary("JAVA-MOD", "Max Beispiel"),
                        new CourseSummary("K8S-GRD", "Max Beispiel"));
    }
}
