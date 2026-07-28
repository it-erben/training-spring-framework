package tech.erben.springboot.basics.testing.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Loesung zu Aufgabe 1: Unit-Test ohne Spring-Kontext. Die
 * {@link MockitoExtension} erzeugt fuer jedes {@code @Mock}-Feld ein
 * Mock-Objekt und injiziert beide in das {@code @InjectMocks}-Feld.
 * Getestet wird die Platz-Bedingung genau an ihrer Grenze: Bei zwei
 * Plaetzen entscheidet die zweite Anmeldung (eine vorhanden → letzter
 * freier Platz) gegen die dritte (zwei vorhanden → voll). Nur so faellt
 * auf, wenn aus {@code <} ein {@code <=} wird.
 */
@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private ParticipantService participantService;

    @Test
    @DisplayName("Der letzte freie Platz wird noch vergeben")
    void registersOnLastFreeSeat() {
        Course course = new Course("SPRING-ADV", "Spring Boot Advanced", 2);
        when(courseRepository.findByCode("SPRING-ADV")).thenReturn(Optional.of(course));
        when(participantRepository.countByCourse(course)).thenReturn(1L);

        boolean registered = participantService.register("SPRING-ADV", "anna@example.com");

        assertThat(registered).isTrue();
        verify(participantRepository).save(any(Participant.class));
    }

    @Test
    @DisplayName("Ist der Kurs voll, kommt false zurueck und nichts wird gespeichert")
    void rejectsWhenCourseIsFull() {
        Course course = new Course("SPRING-ADV", "Spring Boot Advanced", 2);
        when(courseRepository.findByCode("SPRING-ADV")).thenReturn(Optional.of(course));
        when(participantRepository.countByCourse(course)).thenReturn(2L);

        boolean registered = participantService.register("SPRING-ADV", "ben@example.com");

        assertThat(registered).isFalse();
        // Der Rueckgabewert allein reicht nicht: Wuerde der Service trotz
        // vollem Kurs speichern, faende die false-Pruefung das nicht.
        // Erst dieses verify stellt sicher, dass wirklich nichts in der
        // Datenbank landen wuerde.
        verify(participantRepository, never()).save(any(Participant.class));
    }
}
