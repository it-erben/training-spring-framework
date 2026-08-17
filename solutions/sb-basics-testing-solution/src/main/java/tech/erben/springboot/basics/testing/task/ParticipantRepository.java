package tech.erben.springboot.basics.testing.task;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Teilnehmer-Zugriff. {@code countByCourse} zählt die vorhandenen
 * Anmeldungen eines Kurses — die Zahl, gegen die der
 * {@link ParticipantService} die Kapazität prüft.
 */
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    long countByCourse(Course course);
}
