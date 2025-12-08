package tech.erben.springboot;

import org.springframework.context.annotation.Bean;

@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {

    @Bean
    public FileService fileService() {
        return new FileService();
    }

}