package tech.erben.springboot.basics.web.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Aufgabe 1: GET /api/courses liefert 200 und grossFee")
    void listReturnsCoursesWithGrossFee() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].grossFee").exists());
    }

    @Test
    @DisplayName("Aufgabe 2: Unbekannter Kurscode liefert 404")
    void unknownCodeReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/courses/GIBT-ES-NICHT"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Aufgabe 3: POST liefert 201 und Location")
    void createReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"K8S-INTRO",
                                 "title":"Kubernetes Einstieg",
                                 "seats":10,
                                 "netFee":1400.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "/api/courses/K8S-INTRO"));
    }

    @Test
    @DisplayName("Aufgabe 4: POST ohne Titel liefert 400")
    void createWithoutTitleReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"K8S-INTRO",
                                 "title":"",
                                 "seats":10,
                                 "netFee":1400.00}
                                """))
                .andExpect(status().isBadRequest());
    }
}
