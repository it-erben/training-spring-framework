package tech.erben;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@Configuration
public class TestConfig {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest")
        .withExposedPorts(27017);

    @Container
    public static GenericContainer<?> redisContainer = new GenericContainer<>(
        DockerImageName.parse("redis:latest")
    )
        .withExposedPorts(6379);

    static {
        mongoDBContainer.start();
        var mappedPort = mongoDBContainer.getMappedPort(27017);
        System.setProperty("mongodb.container.port", String.valueOf(mappedPort));
        redisContainer.start();
        System.setProperty("spring.data.redis.host", redisContainer.getHost());
        System.setProperty(
            "spring.data.redis.port",
            redisContainer.getMappedPort(6379).toString()
        );
    }

    @Bean
    public PersonService personService(@Autowired PersonRepository personRepository) {
        return new PersonService(personRepository);
    }
}
