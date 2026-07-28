package tech.erben.springboot.basics.core.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private TrainerDirectory trainerDirectory;

    @Test
    @DisplayName("Aufgabe 1: Der Katalog ist als Bean verdrahtet")
    void catalogIsWired() {
        assertThat(courseService.findAll()).hasSize(4);
    }

    @Test
    @DisplayName("Aufgabe 2: Ohne Qualifier greift der Brutto-Rechner")
    void defaultCalculatorAddsVat() {
        Course course = new Course("SB-BASIC", "Spring Boot Basics", 12,
                new BigDecimal("1000.00"));

        assertThat(courseService.feeFor(course))
                .isEqualByComparingTo(new BigDecimal("1190.00"));
    }

    @Test
    @DisplayName("Aufgabe 3: Mit Qualifier greift der Netto-Rechner")
    void qualifiedCalculatorReturnsNetFee() {
        Course course = new Course("SB-BASIC", "Spring Boot Basics", 12,
                new BigDecimal("1000.00"));

        assertThat(courseService.netFeeFor(course))
                .isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("Aufgabe 4: TrainerDirectory kommt aus einer @Bean-Methode")
    void trainerDirectoryIsProvidedByConfiguration() {
        assertThat(trainerDirectory.findByEmail("anna@example.com"))
                .isPresent();
    }
}
