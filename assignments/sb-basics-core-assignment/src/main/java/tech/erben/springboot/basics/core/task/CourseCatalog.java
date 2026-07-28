package tech.erben.springboot.basics.core.task;

import java.util.List;
import java.util.Optional;

/**
 * Zugriff auf den Kurskatalog. Der {@link CourseService} haengt nur von
 * diesem Interface ab — welche Implementierung der Container injiziert,
 * ist ihm egal.
 */
public interface CourseCatalog {

    List<Course> findAll();

    Optional<Course> findByCode(String code);
}
