package tech.erben.springboot.basics.core.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Ein einziger Test für alle vier Aufgaben: Fehlt auch nur eine Bean,
 * startet der Anwendungskontext gar nicht erst. Getrennte Testmethoden
 * würden deshalb ohnehin immer gemeinsam rot oder grün sein.
 *
 * <p>{@code assertAll} führt alle vier Zusicherungen aus, statt bei der
 * ersten Abweichung abzubrechen: Sobald der Kontext startet, zeigt der
 * Fehlerbericht alle noch offenen Aufgaben auf einmal.</p>
 */
@SpringBootTest
class CourseServiceTest {

    private static final Course COURSE = new Course("SB-BASIC", "Spring Boot Basics", 12,
            new BigDecimal("1000.00"));

    @Autowired
    private CourseService courseService;

    @Autowired
    private TrainerDirectory trainerDirectory;

    @Test
    @DisplayName("Die Kursverwaltung ist vollständig verdrahtet")
    void courseAdminIsFullyWired() {
        assertAll(
                () -> assertThat(courseService.findAll())
                        .as("Aufgabe 1: Der Katalog wird als Bean injiziert")
                        .hasSize(4),
                () -> assertThat(courseService.feeFor(COURSE))
                        .as("Aufgabe 2: Ohne Qualifier greift der Brutto-Rechner")
                        .isEqualByComparingTo(new BigDecimal("1190.00")),
                () -> assertThat(courseService.netFeeFor(COURSE))
                        .as("Aufgabe 3: Mit Qualifier greift der Netto-Rechner")
                        .isEqualByComparingTo(new BigDecimal("1000.00")),
                () -> assertThat(trainerDirectory.findByEmail("anna@example.com"))
                        .as("Aufgabe 4: TrainerDirectory kommt aus einer @Bean-Methode")
                        .isPresent());
    }
}
