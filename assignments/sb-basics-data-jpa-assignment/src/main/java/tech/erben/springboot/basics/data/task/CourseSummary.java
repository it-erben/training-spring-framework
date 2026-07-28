package tech.erben.springboot.basics.data.task;

/**
 * Projektion fuer Aufgabe 3: statt kompletter Entities nur Kurscode und
 * Trainername. Das Record ist fertig — die JPQL-Query, die es per
 * {@code select new} befuellt, schreibt ihr im {@link CourseRepository}.
 */
public record CourseSummary(String code, String trainerName) {
}
