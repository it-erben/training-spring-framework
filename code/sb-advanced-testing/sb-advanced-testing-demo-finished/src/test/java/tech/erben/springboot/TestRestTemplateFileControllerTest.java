package tech.erben.springboot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestRestTemplateFileControllerTest {

    @MockBean
    MockMeBean mmb;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testHelloIntegration() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/files?id=test",
            String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(response);
    }
}
