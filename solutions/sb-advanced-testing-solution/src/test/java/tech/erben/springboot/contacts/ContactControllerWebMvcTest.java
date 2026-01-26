package tech.erben.springboot.contacts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
class ContactControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactService contactService;

    @Test
    void postContacts_returns201_whenValid() throws Exception {
        // Given
        Contact savedContact = new Contact("Max", "Mustermann", "max.mustermann@example.com");
        savedContact.setId(1L);

        when(contactService.create(any(ContactCreateDto.class))).thenReturn(savedContact);

        String requestJson = """
                {
                    "firstName": "Max",
                    "lastName": "Mustermann",
                    "email": "max.mustermann@example.com"
                }
                """;

        // When & Then
        mockMvc.perform(post("/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Max"))
                .andExpect(jsonPath("$.lastName").value("Mustermann"))
                .andExpect(jsonPath("$.email").value("max.mustermann@example.com"));
    }

    @Test
    void getContacts_delegatesToService_andReturns200() throws Exception {
        // Given
        Contact contact1 = new Contact("Max", "Schmidt", "max.schmidt@example.com");
        contact1.setId(1L);
        Contact contact2 = new Contact("Anna", "Schneider", "anna.schneider@example.com");
        contact2.setId(2L);

        when(contactService.searchByLastNamePrefix("Sch")).thenReturn(List.of(contact1, contact2));

        // When & Then
        mockMvc.perform(get("/contacts")
                        .param("lastNamePrefix", "Sch")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].lastName").value("Schmidt"))
                .andExpect(jsonPath("$[1].lastName").value("Schneider"));
    }

    @Test
    void getContacts_returnsEmptyList_whenNoMatches() throws Exception {
        // Given
        when(contactService.searchByLastNamePrefix("XYZ")).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/contacts")
                        .param("lastNamePrefix", "XYZ")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
