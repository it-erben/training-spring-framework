package tech.erben.springboot.basics.data.task;

/**
 * Projektion für Aufgabe 3: statt kompletter Entities nur Kurscode und
 * Trainername. Befüllt wird das Record per {@code select new} in
 * {@link CourseRepository#findSummaries()}.
 */
public record CourseSummary(String code, String trainerName) {
}
