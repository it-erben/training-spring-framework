package tech.erben.springboot.basics.testing.task;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Legt beim Start zwei Kurse an — gegen die laeuft euer Integrationstest
 * aus Aufgabe 3: {@code SPRING-BASICS} hat reichlich Plaetze,
 * {@code SPRING-COMPACT} genau einen. Im {@code @WebMvcTest}-Slice wird
 * diese Klasse nicht geladen: Der Slice startet nur Controller und
 * Advice-Klassen, keine normalen Components.
 */
@Component
public class CourseSeeder implements CommandLineRunner {

    private final CourseRepository courseRepository;

    public CourseSeeder(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(String... args) {
        courseRepository.save(new Course("SPRING-BASICS", "Spring Boot Grundlagen", 12));
        courseRepository.save(new Course("SPRING-COMPACT", "Spring Boot kompakt", 1));
    }
}
