package tech.erben.springboot.basics.data.task;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * JPA-Entity für einen Trainer — war bereits fertig gemappt und ist
 * unverändert geblieben.
 */
@Entity
public class Trainer {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String email;

    /** Von JPA gefordert — Hibernate instanziiert Entities über diesen Konstruktor. */
    protected Trainer() {
    }

    public Trainer(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
