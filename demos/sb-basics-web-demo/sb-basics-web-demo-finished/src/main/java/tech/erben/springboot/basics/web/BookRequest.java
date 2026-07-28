package tech.erben.springboot.basics.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Eingabe-DTO fuer {@code POST /api/books}. Die Bean-Validation-Annotationen
 * beschreiben, was ein gueltiger Request ist — geprueft werden sie erst,
 * wenn der Controller-Parameter mit {@code @Valid} markiert ist.
 */
public record BookRequest(
        @NotBlank String isbn,
        @NotBlank @Size(max = 200) String title,
        @NotNull @Positive BigDecimal netPrice) {

    /** Uebersetzt das Eingabe-DTO in das interne Domaenenmodell. */
    public Book toBook() {
        return new Book(isbn, title, netPrice);
    }
}
