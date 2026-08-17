package tech.erben.springboot.basics.web.task;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Lösung Aufgabe 1: Ausgabe-DTO der REST-Schnittstelle. Zusätzlich zum
 * Domänenmodell enthält es die berechnete Bruttogebühr (19 %
 * Mehrwertsteuer) — ein typischer Grund, nach außen ein eigenes DTO
 * statt der internen Klasse zu verwenden.
 */
public record CourseResponse(String code, String title, int seats,
                             BigDecimal netFee, BigDecimal grossFee) {

    private static final BigDecimal VAT_FACTOR = new BigDecimal("1.19");

    public static CourseResponse from(Course course) {
        BigDecimal grossFee = course.netFee()
                .multiply(VAT_FACTOR)
                .setScale(2, RoundingMode.HALF_UP);
        return new CourseResponse(course.code(), course.title(),
                course.seats(), course.netFee(), grossFee);
    }
}
