package tech.erben.springboot.basics.testing.task;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Ein angemeldeter Teilnehmer. {@code @ManyToOne} macht aus der
 * Objektreferenz die Fremdschluessel-Spalte {@code course_id} — viele
 * Teilnehmer, ein Kurs.
 */
@Entity
public class Participant {

    @Id
    @GeneratedValue
    private Long id;

    private String email;

    @ManyToOne
    private Course course;

    /** Von JPA gefordert — Hibernate instanziiert Entities ueber diesen Konstruktor. */
    protected Participant() {
    }

    public Participant(String email, Course course) {
        this.email = email;
        this.course = course;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Course getCourse() {
        return course;
    }
}
