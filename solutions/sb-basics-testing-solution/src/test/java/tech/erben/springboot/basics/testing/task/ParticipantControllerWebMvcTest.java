package tech.erben.springboot.basics.testing.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lösung zu Aufgabe 2: Slice-Test. {@code @WebMvcTest} startet nur die
 * MVC-Schicht — Controller, Advice-Klassen und die JSON-Serialisierung.
 * Der {@link ParticipantService} wird per {@code @MockitoBean} ersetzt
 * (der Nachfolger von {@code @MockBean}, das es in Spring Boot 4 nicht
 * mehr gibt): Was das Mock zurückgibt, entscheidet der Test — geprüft
 * wird nur noch die Übersetzung in Statuscodes.
 */
@WebMvcTest(ParticipantController.class)
class ParticipantControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParticipantService participantService;

    @Test
    @DisplayName("Erfolgreiche Anmeldung liefert 201 mit Location-Header")
    void returnsCreatedWhenRegistrationSucceeds() throws Exception {
        when(participantService.register("SPRING-ADV", "anna@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/courses/SPRING-ADV/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"anna@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/courses/SPRING-ADV/participants"));
    }

    @Test
    @DisplayName("Ausgebuchter Kurs liefert 409 Conflict")
    void returnsConflictWhenCourseIsFull() throws Exception {
        when(participantService.register("SPRING-ADV", "ben@example.com")).thenReturn(false);

        mockMvc.perform(post("/api/courses/SPRING-ADV/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"ben@example.com\"}"))
                .andExpect(status().isConflict());
    }
}
