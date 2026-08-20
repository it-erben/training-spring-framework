package tech.erben.springboot.basics.recap.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-Slice aus Modul 04: {@code @WebMvcTest} startet nur die MVC-Schicht
 * samt Advice und JSON-Serialisierung. Der {@link RoomService} kommt als
 * {@code @MockitoBean} dazu — der Nachfolger von {@code @MockBean}, das es
 * in Spring Boot 4 nicht mehr gibt.
 */
@WebMvcTest(RoomController.class)
class RoomControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @Test
    @DisplayName("GET /api/rooms?minSeats=50 reicht den Filter an den Service durch")
    void listsRoomsWithMinimumSeats() throws Exception {
        when(roomService.findWithAtLeast(50))
                .thenReturn(List.of(new Room("Aula", 120, "Haus A")));

        mockMvc.perform(get("/api/rooms").param("minSeats", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Aula"))
                .andExpect(jsonPath("$[0].seats").value(120));
    }

    @Test
    @DisplayName("Unbekannte ID liefert 404 über den RestExceptionHandler")
    void returnsNotFoundForUnknownId() throws Exception {
        when(roomService.findById(42L)).thenThrow(new RoomNotFoundException(42L));

        mockMvc.perform(get("/api/rooms/42"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/rooms legt an und liefert 201")
    void createsRoom() throws Exception {
        when(roomService.create(eq("Seminar 2"), eq(16), any()))
                .thenReturn(new Room("Seminar 2", 16, "Haus A"));

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Seminar 2\", \"seats\": 16}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.building").value("Haus A"));
    }

    @Test
    @DisplayName("Leerer Name verletzt die Validierung und liefert 400")
    void rejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\", \"seats\": 16}"))
                .andExpect(status().isBadRequest());
    }
}
