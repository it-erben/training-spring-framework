package tech.erben.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class SerializationTest {

    @Autowired
    private JacksonTester<UserEntity> json;

    @Test
    public void shouldSerializeDateCorrectly() throws IOException {
        UserEntity value = new UserEntity();
        value.setBirthday(LocalDate.of(1990,01,01));
        var content = json.write(value);
        assertThat(content)
                .extractingJsonPathStringValue("$.birthday")
                .isEqualTo("01.01.1990");
    }
}
