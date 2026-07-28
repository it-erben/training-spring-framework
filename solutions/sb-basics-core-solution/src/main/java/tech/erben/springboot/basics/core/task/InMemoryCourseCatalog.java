package tech.erben.springboot.basics.core.task;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * In-Memory-Implementierung mit vier festen Kursen. {@code @Repository}
 * macht die Klasse zur Bean — der Container instanziiert sie und injiziert
 * sie ueberall dort, wo ein {@link CourseCatalog} verlangt wird.
 */
@Repository
public class InMemoryCourseCatalog implements CourseCatalog {

    private final List<Course> courses = List.of(
            new Course("SB-BASIC", "Spring Boot Grundlagen", 12, new BigDecimal("1990.00")),
            new Course("SB-ADV", "Spring Boot Advanced", 10, new BigDecimal("2490.00")),
            new Course("JAVA-21", "Modernes Java 21", 12, new BigDecimal("1790.00")),
            new Course("K8S-INTRO", "Kubernetes fuer Entwickler", 8, new BigDecimal("2190.00"))
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
