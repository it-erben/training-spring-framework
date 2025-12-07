package tech.erben.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

@JsonTest
class PersonJsonTest {

    @Autowired
    private JacksonTester<Person> json;

    @Test
    void serializesPerson() throws IOException {
        Person person = new Person();
        person.setId(1L);
        person.setName("Alice");
        person.setAge(30);

        JsonContent<Person> result = json.write(person);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Alice");
        assertThat(result).extractingJsonPathNumberValue("$.age").isEqualTo(30);
    }
}
