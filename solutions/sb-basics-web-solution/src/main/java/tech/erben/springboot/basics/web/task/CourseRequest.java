package tech.erben.springboot.basics.web.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Loesung Aufgabe 3 und 4: Eingabe-DTO fuer {@code POST /api/courses}.
 * Die Bean-Validation-Annotationen beschreiben, was ein gueltiger Request
 * ist — geprueft werden sie erst, wenn der Controller-Parameter mit
 * {@code @Valid} markiert ist.
 */
public record CourseRequest(
        @NotBlank String code,
        @NotBlank @Size(max = 200) String title,
        @Positive int seats,
        @NotNull @Positive BigDecimal netFee) {

    /** Uebersetzt das Eingabe-DTO in das interne Domaenenmodell. */
    public Course toCourse() {
        return new Course(code, title, seats, netFee);
    }
}
