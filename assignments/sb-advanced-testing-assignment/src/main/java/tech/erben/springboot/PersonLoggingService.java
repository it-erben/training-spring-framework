package tech.erben.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PersonLoggingService {

    private static final Logger log = LoggerFactory.getLogger(PersonLoggingService.class);

    public void recordCreated(Person person) {
        log.info("Person created: {} ({})", person.getName(), person.getAge());
    }
}
