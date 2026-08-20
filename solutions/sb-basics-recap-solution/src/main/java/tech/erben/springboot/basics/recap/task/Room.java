package tech.erben.springboot.basics.recap.task;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * Ein Schulungsraum. {@code name} ist fachlicher Schlüssel und deshalb
 * eindeutig; {@code seats} trägt die Kapazität, nach der die Suche filtert.
 */
@Entity
public class Room {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String name;

    private int seats;
    private String building;

    /** Von JPA gefordert — Hibernate instanziiert Entities über diesen Konstruktor. */
    protected Room() {
    }

    public Room(String name, int seats, String building) {
        this.name = name;
        this.seats = seats;
        this.building = building;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSeats() {
        return seats;
    }

    public String getBuilding() {
        return building;
    }
}
