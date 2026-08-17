package tech.erben.springboot.basics.testing;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * JPA-Entity für ein Buch — dasselbe Modell wie in Modul 03, bewusst
 * kompakt: keine Beziehungen, nur der (implizite) parameterlose
 * Konstruktor plus Getter und Setter. So können auch die Tests ihre
 * Objekte bequem aufbauen. Die Bean-Validation-Annotationen stehen direkt
 * am Modell, weil der Controller die Entity ohne eigenes DTO
 * entgegennimmt — in einer echten Anwendung wären getrennte DTOs wie in
 * Modul 02 die bessere Wahl, hier liegt der Fokus auf den Tests.
 */
@Entity
public class Book {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    private String isbn;

    @NotBlank
    private String title;

    @NotNull
    @Positive
    private BigDecimal netPrice;

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getNetPrice() {
        return netPrice;
    }

    public void setNetPrice(BigDecimal netPrice) {
        this.netPrice = netPrice;
    }
}
