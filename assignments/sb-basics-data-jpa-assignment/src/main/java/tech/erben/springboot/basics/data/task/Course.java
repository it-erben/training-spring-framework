package tech.erben.springboot.basics.data.task;

import java.math.BigDecimal;

/**
 * Ein Kurs aus dem Schulungskatalog. Felder, Konstruktoren und Getter sind
 * fertig — aber JPA kennt diese Klasse noch nicht: Es fehlt jede Annotation.
 * Genau daran scheitert der Start des Spring-Kontexts
 * ({@code Not a managed type}), denn das {@link CourseRepository} verlangt
 * eine Entity. Aufgabe 1 macht aus der Klasse eine.
 */
// TODO: Modul 03 — Aufgabe 1: Klasse mit @Entity als JPA-Entity markieren
public class Course {

    // TODO: Modul 03 — Aufgabe 1: id mit @Id und @GeneratedValue zum Primaerschluessel machen
    private Long id;

    // TODO: Modul 03 — Aufgabe 1: code mit @Column(unique = true) gegen Duplikate absichern
    private String code;

    private String title;
    private int seats;
    private BigDecimal netFee;

    // TODO: Modul 03 — Aufgabe 1: trainer mit @ManyToOne als Beziehung mappen (daraus wird die Spalte trainer_id)
    private Trainer trainer;

    /** Von JPA gefordert — Hibernate instanziiert Entities ueber diesen Konstruktor. */
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
