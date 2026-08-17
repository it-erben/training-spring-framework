package tech.erben.springboot.basics.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA-Entity für einen Autor. Die Gegenseite der Beziehung: {@code mappedBy}
 * sagt Hibernate, dass die Fremdschlüssel-Spalte bereits durch
 * {@link Book#getAuthor()} definiert ist — sonst entstünde eine zweite,
 * überflüssige Verknüpfungstabelle.
 */
@Entity
public class Author {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "author")
    private List<Book> books = new ArrayList<>();

    /** Von JPA gefordert — Hibernate instanziiert Entities über diesen Konstruktor. */
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
