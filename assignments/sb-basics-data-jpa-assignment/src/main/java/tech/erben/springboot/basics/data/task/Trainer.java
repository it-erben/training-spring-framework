package tech.erben.springboot.basics.data.task;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * JPA-Entity fuer einen Trainer — fertig gemappt, als Vorlage fuer
 * Aufgabe 1: {@code @Entity} macht die Klasse zur Tabelle, {@code @Id}
 * mit {@code @GeneratedValue} den Primaerschluessel zur Sache der
 * Datenbank. Hier aendert ihr nichts.
 */
@Entity
public class Trainer {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String email;

    /** Von JPA gefordert — Hibernate instanziiert Entities ueber diesen Konstruktor. */
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
