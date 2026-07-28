package tech.erben.springboot.basics.testing.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Kursverwaltung. Die Startdaten legt der
 * {@link CourseSeeder} an — bewusst als eigene Klasse, damit die
 * Slice-Tests ({@code @WebMvcTest}) ihn gar nicht erst laden.
 */
@SpringBootApplication
public class CourseAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseAdminApplication.class, args);
    }
}
