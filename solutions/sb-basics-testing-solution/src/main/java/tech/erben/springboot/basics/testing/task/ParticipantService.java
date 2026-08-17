package tech.erben.springboot.basics.testing.task;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fachlogik der Anmeldung. Das interessanteste Stück ist die
 * Platz-Bedingung in {@link #register(String, String)}: angemeldet wird
 * nur, solange die Zahl der vorhandenen Anmeldungen unter der Kapazität
 * des Kurses liegt. Genau solche Regeln mit Grenzfällen (vorletzter
 * Platz? letzter? voll?) sind der klassische Fall für schnelle
 * Unit-Tests ohne Spring-Kontext.
 */
@Service
public class ParticipantService {

    private final CourseRepository courseRepository;
    private final ParticipantRepository participantRepository;

    public ParticipantService(CourseRepository courseRepository,
                              ParticipantRepository participantRepository) {
        this.courseRepository = courseRepository;
        this.participantRepository = participantRepository;
    }

    /**
     * Meldet {@code email} zum Kurs mit {@code courseCode} an, solange
     * freie Plätze da sind. Gibt {@code false} zurück, wenn der Kurs
     * voll ist; bei unbekanntem Code fliegt eine
     * {@link CourseNotFoundException}.
     */
    @Transactional
    public boolean register(String courseCode, String email) {
        Course course = courseRepository.findByCode(courseCode)
                .orElseThrow(() -> new CourseNotFoundException(courseCode));
        long registered = participantRepository.countByCourse(course);
        if (registered < course.getSeats()) {
            participantRepository.save(new Participant(email, course));
            return true;
        }
        return false;
    }
}
