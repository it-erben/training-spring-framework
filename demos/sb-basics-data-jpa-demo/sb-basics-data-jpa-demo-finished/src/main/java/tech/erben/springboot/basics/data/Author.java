package tech.erben.springboot.basics.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA-Entity fuer einen Autor. Die Gegenseite der Beziehung: {@code mappedBy}
 * sagt Hibernate, dass die Fremdschluessel-Spalte bereits durch
 * {@link Book#getAuthor()} definiert ist — sonst entstuende eine zweite,
 * ueberfluessige Verknuepfungstabelle.
 */
@Entity
public class Author {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "author")
    private List<Book> books = new ArrayList<>();

    /** Von JPA gefordert — Hibernate instanziiert Entities ueber diesen Konstruktor. */
    protected Author() {
    }

    public Author(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Book> getBooks() {
        return books;
    }
}
