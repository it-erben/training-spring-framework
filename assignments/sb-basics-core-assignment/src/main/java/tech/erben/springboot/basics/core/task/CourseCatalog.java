package tech.erben.springboot.basics.core.task;

import java.util.List;
import java.util.Optional;

/**
 * Zugriff auf den Kurskatalog. Der {@link CourseService} hängt nur von
 * diesem Interface ab. Welche Implementierung der Container injiziert,
 * ist ihm gleich.
 */
public interface CourseCatalog {

    List<Course> findAll();

    Optional<Course> findByCode(String code);
}
