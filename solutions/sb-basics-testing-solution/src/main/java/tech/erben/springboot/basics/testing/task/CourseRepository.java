package tech.erben.springboot.basics.testing.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Kurs-Zugriff wie in Modul 03: {@code findByCode} ist eine Derived
 * Query, Spring Data leitet das SQL aus dem Methodennamen ab.
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCode(String code);
}
