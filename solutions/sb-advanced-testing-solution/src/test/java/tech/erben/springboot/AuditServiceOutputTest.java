package tech.erben.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class AuditServiceOutputTest {

    @Autowired
    PersonLoggingService loggingService;

    @Test
    void capturesLogOutput(CapturedOutput output) {
        Person person = new Person();
        person.setName("Log User");
        person.setAge(20);

        loggingService.recordCreated(person);

        assertThat(output).contains("Person created: Log User (20)");
    }
}
