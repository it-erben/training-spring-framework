package tech.erben.springboot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.erben.springboot.HelloService;

@SpringBootTest
public class ApplicationContextTest {

    @Autowired
    private HelloService helloService;

    @Test
    public void basicTest() {
        Assertions.assertEquals("welcome", helloService.sayHello());
    }

}
