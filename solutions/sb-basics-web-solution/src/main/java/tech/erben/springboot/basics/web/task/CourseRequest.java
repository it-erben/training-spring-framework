package tech.erben.springboot.basics.web.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Lösung Aufgabe 3 und 4: Eingabe-DTO für {@code POST /api/courses}.
 * Die Bean-Validation-Annotationen beschreiben, was ein gültiger Request
 * ist — geprüft werden sie erst, wenn der Controller-Parameter mit
 * {@code @Valid} markiert ist.
 */
public record CourseRequest(
        @NotBlank String code,
        @NotBlank @Size(max = 200) String title,
        @Positive int seats,
        @NotNull @Positive BigDecimal netFee) {

    /** Übersetzt das Eingabe-DTO in das interne Domänenmodell. */
    public Course toCourse() {
        return new Course(code, title, seats, netFee);
    }
}
