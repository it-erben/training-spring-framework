package tech.erben.springboot.basics.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Eingabe-DTO für {@code POST /api/books}. Die Bean-Validation-Annotationen
 * beschreiben, was ein gültiger Request ist — geprüft werden sie erst,
 * wenn der Controller-Parameter mit {@code @Valid} markiert ist.
 */
public record BookRequest(
        @NotBlank String isbn,
        @NotBlank @Size(max = 200) String title,
        @NotNull @Positive BigDecimal netPrice) {

    /** Übersetzt das Eingabe-DTO in das interne Domänenmodell. */
    public Book toBook() {
        return new Book(isbn, title, netPrice);
    }
}
