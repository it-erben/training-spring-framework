package tech.erben.springboot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HelloService {

    @Value("${tech.erben.springboot.testing.helloService.message}")
    private String message;

    public String sayHello() {
        return message;
    }
}
