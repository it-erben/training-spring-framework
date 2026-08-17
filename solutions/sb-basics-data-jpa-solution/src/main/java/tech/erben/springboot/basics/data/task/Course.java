package tech.erben.springboot.basics.data.task;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

/**
 * Lösung zu Aufgabe 1: {@code @Entity} macht die Klasse zur Tabelle
 * {@code course}, {@code @Id} mit {@code @GeneratedValue} überlässt die
 * Schlüsselvergabe der Datenbank, {@code @Column(unique = true)} legt
 * einen Unique-Constraint auf den Kurscode, und {@code @ManyToOne} macht
 * aus der Objektreferenz die Fremdschlüssel-Spalte {@code trainer_id}.
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
    private BigDecimal netFee;

    @ManyToOne
    private Trainer trainer;

    /** Von JPA gefordert — Hibernate instanziiert Entities über diesen Konstruktor. */
    protected Course() {
    }

    public Course(String code, String title, int seats, BigDecimal netFee, Trainer trainer) {
        this.code = code;
        this.title = title;
        this.seats = seats;
        this.netFee = netFee;
        this.trainer = trainer;
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

    public BigDecimal getNetFee() {
        return netFee;
    }

    public void setNetFee(BigDecimal netFee) {
        this.netFee = netFee;
    }

    public Trainer getTrainer() {
        return trainer;
    }
}
