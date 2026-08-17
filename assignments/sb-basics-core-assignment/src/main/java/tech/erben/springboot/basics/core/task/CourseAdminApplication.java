package tech.erben.springboot.basics.core.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Kursverwaltung. {@code @SpringBootApplication}
 * aktiviert das Component-Scanning ab diesem Package. Beans entstehen
 * daraus aber erst, wenn die Klassen auch als Beans deklariert sind.
 * Das ist eure Aufgabe.
 */
@SpringBootApplication
public class CourseAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseAdminApplication.class, args);
    }
}
