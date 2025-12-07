package tech.erben.springboot;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PersonController.class)
class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personService;

    @Test
    void getAllPersons() throws Exception {
        Person person1 = new Person();
        person1.setName("Alice");
        person1.setAge(30);

        Person person2 = new Person();
        person2.setName("Jane Doe");
        person2.setAge(29);

        List<Person> personList = Arrays.asList(person1, person2);
        when(personService.getAllPersons()).thenReturn(personList);

        mockMvc
            .perform(get("/api/person"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name").value("Alice"))
            .andExpect(jsonPath("$[0].age").value(30));
    }
}
