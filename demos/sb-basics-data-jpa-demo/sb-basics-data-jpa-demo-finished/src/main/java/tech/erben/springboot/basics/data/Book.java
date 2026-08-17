package tech.erben.springboot.basics.data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

/**
 * JPA-Entity für ein Buch. Anders als die Records aus Modul 02 braucht
 * eine Entity einen parameterlosen Konstruktor und veränderbare Felder —
 * Hibernate erzeugt Instanzen per Reflection und schreibt die Spaltenwerte
 * direkt hinein. Aus der Klasse entsteht per {@code ddl-auto=create-drop}
 * beim Start die Tabelle {@code book}.
 */
@Entity
public class Book {

    @Id
    @GeneratedValue
    private Long id;

    private String isbn;
    private String title;
    private BigDecimal netPrice;

    /**
     * Viele Bücher gehören zu einem Autor — daraus entsteht die
     * Fremdschlüssel-Spalte {@code author_id}. Das Cascade sorgt dafür,
     * dass ein noch nicht gespeicherter Autor beim Speichern des Buchs
     * mitgespeichert wird (bequem für das Seeding im
     * {@link SeedDataRunner}).
     */
    @ManyToOne(cascade = CascadeType.PERSIST)
    private Author author;

    /** Von JPA gefordert — Hibernate instanziiert Entities über diesen Konstruktor. */
    protected Book() {
    }

    public Book(String isbn, String title, BigDecimal netPrice, Author author) {
        this.isbn = isbn;
        this.title = title;
        this.netPrice = netPrice;
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getNetPrice() {
        return netPrice;
    }

    public void setNetPrice(BigDecimal netPrice) {
        this.netPrice = netPrice;
    }

    public Author getAuthor() {
        return author;
    }
}
