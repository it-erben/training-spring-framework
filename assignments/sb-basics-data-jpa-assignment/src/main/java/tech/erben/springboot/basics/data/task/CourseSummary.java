package tech.erben.springboot.basics.data.task;

/**
 * Projektion für Aufgabe 3: statt kompletter Entities nur Kurscode und
 * Trainername. Das Record ist fertig.Die JPQL-Query, die es per
 * {@code select new} befüllt, schreibt ihr im {@link CourseRepository}.
 */
public record CourseSummary(String code, String trainerName) {
}
