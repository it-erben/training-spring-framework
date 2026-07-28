package tech.erben.springboot.basics.web.task;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-Memory-Verwaltung des Kurskatalogs. Die Fachlogik weiss nichts von
 * HTTP, JSON oder Statuscodes: Sie arbeitet nur mit {@link Course} und
 * wirft bei unbekanntem Kurscode eine {@link CourseNotFoundException}.
 */
@Service
public class CourseService {

    /**
     * Eine synchronisierte {@link LinkedHashMap} reicht fuer die Uebung:
     * Sie behaelt die Einfuegereihenfolge, damit die Ausgabe von Aufruf
     * zu Aufruf stabil bleibt.
     */
    private final Map<String, Course> courses =
            Collections.synchronizedMap(new LinkedHashMap<>());

    public CourseService() {
        create(new Course("SB-BASIC", "Spring Boot Grundlagen", 12,
                new BigDecimal("1990.00")));
        create(new Course("JAVA-21", "Modernes Java 21", 12,
                new BigDecimal("1790.00")));
    }

    public List<Course> findAll() {
        return List.copyOf(courses.values());
    }

    public Course findByCode(String code) {
        Course course = courses.get(code);
        if (course == null) {
            throw new CourseNotFoundException(code);
        }
        return course;
    }

    public Course create(Course course) {
        courses.put(course.code(), course);
        return course;
    }

    /** Ersetzt einen vorhandenen Kurs — genutzt von der Bonusaufgabe (PUT). */
    public Course update(String code, Course course) {
        if (!courses.containsKey(code)) {
            throw new CourseNotFoundException(code);
        }
        courses.put(code, course);
        return course;
    }

    public void delete(String code) {
        if (courses.remove(code) == null) {
            throw new CourseNotFoundException(code);
        }
    }
}
