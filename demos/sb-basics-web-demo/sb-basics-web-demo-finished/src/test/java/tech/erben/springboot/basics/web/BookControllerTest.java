package tech.erben.springboot.basics.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/books liefert 200 und den Bruttopreis")
    void listReturnsBooksWithGrossPrice() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].grossPrice").exists());
    }

    @Test
    @DisplayName("GET auf unbekannte ISBN liefert 404")
    void unknownIsbnReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/books/999-does-not-exist"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST mit gueltigem Body liefert 201 und Location")
    void createReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"978-0-13-468599-1",
                                 "title":"Effective Java",
                                 "netPrice":49.99}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "/api/books/978-0-13-468599-1"));
    }

    @Test
    @DisplayName("POST ohne Titel liefert 400")
    void createWithoutTitleReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"978-0-13-468599-1",
                                 "title":"",
                                 "netPrice":49.99}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET auf bekannte ISBN liefert 200 mit 19 % Bruttopreis")
    void knownIsbnReturnsBookWithGrossPrice() throws Exception {
        mockMvc.perform(get("/api/books/978-3-8362-9049-8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("Spring Boot 3 und Spring Framework 6"))
                .andExpect(jsonPath("$.netPrice").value(49.90))
                // 49.90 * 1.19 = 59.381, kaufmaennisch gerundet 59.38
                .andExpect(jsonPath("$.grossPrice").value(59.38));
    }

    @Test
    @DisplayName("POST mit negativem Preis liefert 400 mit Feldfehler")
    void createWithNegativePriceReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"978-0-32-135668-0",
                                 "title":"Effective Java (2nd Edition)",
                                 "netPrice":-1.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.netPrice").exists());
    }

    @Test
    @DisplayName("DELETE auf bekannte ISBN liefert 204, danach ist das Buch weg")
    void deleteRemovesBook() throws Exception {
        // Eigenes Buch anlegen, damit der Test unabhaengig von den
        // Seed-Daten und der Ausfuehrungsreihenfolge bleibt.
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"isbn":"978-1-4919-5038-8",
                                 "title":"Head First Java",
                                 "netPrice":39.90}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/books/978-1-4919-5038-8"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/978-1-4919-5038-8"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE auf unbekannte ISBN liefert 404")
    void deleteUnknownIsbnReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/books/999-does-not-exist"))
                .andExpect(status().isNotFound());
    }
}
