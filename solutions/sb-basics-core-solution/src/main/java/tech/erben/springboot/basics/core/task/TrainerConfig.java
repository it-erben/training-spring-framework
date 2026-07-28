package tech.erben.springboot.basics.core.task;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Stellt das {@link TrainerDirectory} als Bean bereit. Die Klasse steht
 * stellvertretend fuer eine fremde Bibliotheksklasse und laesst sich nicht
 * annotieren — die {@code @Bean}-Methode uebernimmt stattdessen die
 * Registrierung im Container.
 */
@Configuration
public class TrainerConfig {

    @Bean
    public TrainerDirectory trainerDirectory() {
        TrainerDirectory directory = new TrainerDirectory();
        directory.register(new Trainer("Anna Schmidt", "anna@example.com"));
        directory.register(new Trainer("Ben Weber", "ben@example.com"));
        return directory;
    }
}
