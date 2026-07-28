package tech.erben.springboot.basics.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice-Test: {@code @WebMvcTest} startet nur die MVC-Schicht — Controller,
 * Advice-Klassen und die JSON-Serialisierung. Kein Service, kein
 * Repository, keine Datenbank: Der {@link BookService} wird per
 * {@code @MockitoBean} ersetzt (der Nachfolger von {@code @MockBean},
 * das es in Spring Boot 4 nicht mehr gibt). Requests laufen ueber
 * {@link MockMvc} durch den echten {@code DispatcherServlet}-Stack,
 * aber ohne HTTP-Server.
 */
@WebMvcTest(BookController.class)
class BookControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    @DisplayName("GET /api/books/{isbn} liefert 200 und das Buch als JSON")
    void getReturnsBookAsJson() throws Exception {
        Book book = new Book();
        book.setIsbn("978-0-13-468599-1");
        book.setTitle("Effective Java");
        book.setNetPrice(new BigDecimal("44.99"));
        when(bookService.findByIsbn("978-0-13-468599-1")).thenReturn(book);

        mockMvc.perform(get("/api/books/978-0-13-468599-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("978-0-13-468599-1"))
                .andExpect(jsonPath("$.title").value("Effective Java"))
                .andExpect(jsonPath("$.netPrice").value(44.99));
    }

    @Test
    @DisplayName("Unbekannte ISBN liefert 404 mit Fehlermeldung im Body")
    void unknownIsbnReturnsNotFound() throws Exception {
        when(bookService.findByIsbn("999-does-not-exist"))
                .thenThrow(new BookNotFoundException("999-does-not-exist"));

        mockMvc.perform(get("/api/books/999-does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Kein Buch mit ISBN 999-does-not-exist gefunden"));
    }
}
