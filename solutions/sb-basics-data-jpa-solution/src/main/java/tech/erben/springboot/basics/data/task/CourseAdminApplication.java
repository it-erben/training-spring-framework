package tech.erben.springboot.basics.data.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Kursverwaltung. Wie in der Demo gibt es keinen
 * Web-Server: Die Anwendung startet, fährt den Persistenz-Stack hoch und
 * dann wieder herunter. Gearbeitet wird in dieser Übung über die Tests —
 * {@code CourseRepositoryTest} misst euren Fortschritt.
 */
@SpringBootApplication
public class CourseAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseAdminApplication.class, args);
    }
}
