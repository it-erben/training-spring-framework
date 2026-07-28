package tech.erben.springboot.basics.web.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Einstiegspunkt der Kursverwaltung. Durch {@code spring-boot-starter-web}
 * startet hier ein eingebetteter Tomcat auf Port 8080 — die
 * REST-Schnittstelle dazu liefert der {@link CourseController}.
 */
@SpringBootApplication
public class CourseAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseAdminApplication.class, args);
    }
}
