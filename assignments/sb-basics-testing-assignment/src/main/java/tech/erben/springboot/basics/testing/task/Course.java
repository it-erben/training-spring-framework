package tech.erben.springboot.basics.testing.task;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * Die Kurs-Entity aus Modul 03, reduziert auf das, was diese Übung
 * braucht: {@code seats} legt fest, wie viele Anmeldungen der Kurs
 * maximal verträgt.
 */
@Entity
public class Course {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String code;

    private String title;
    private int seats;

    /** Von JPA gefordert, Hibernate instanziiert Entities über diesen Konstruktor. */
    protected Course() {
    }

    public Course(String code, String title, int seats) {
        this.code = code;
        this.title = title;
        this.seats = seats;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public int getSeats() {
        return seats;
    }
}
