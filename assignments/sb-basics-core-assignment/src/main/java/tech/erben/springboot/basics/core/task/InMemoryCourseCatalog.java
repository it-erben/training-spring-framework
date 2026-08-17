package tech.erben.springboot.basics.core.task;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * In-Memory-Implementierung mit vier festen Kursen. Die Logik ist fertig,
 * aber der Container kennt die Klasse noch nicht.
 */
// TODO Aufgabe 1: als Spring-Bean deklarieren (Stereotyp-Annotation)
public class InMemoryCourseCatalog implements CourseCatalog {

    private final List<Course> courses = List.of(
            new Course("SB-BASIC", "Spring Boot Grundlagen", 12, new BigDecimal("1990.00")),
            new Course("SB-ADV", "Spring Boot Advanced", 10, new BigDecimal("2490.00")),
            new Course("JAVA-21", "Modernes Java 21", 12, new BigDecimal("1790.00")),
            new Course("K8S-INTRO", "Kubernetes für Entwickler", 8, new BigDecimal("2190.00"))
    );

    @Override
    public List<Course> findAll() {
        return courses;
    }

    @Override
    public Optional<Course> findByCode(String code) {
        return courses.stream()
                .filter(course -> course.code().equals(code))
                .findFirst();
    }
}
