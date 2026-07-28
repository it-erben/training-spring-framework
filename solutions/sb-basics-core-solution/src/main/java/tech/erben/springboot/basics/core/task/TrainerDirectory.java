package tech.erben.springboot.basics.core.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Trainerverzeichnis — steht hier stellvertretend fuer eine Klasse aus
 * einer fremden Bibliothek: Ihr koennt (bzw. duerft) den Quelltext nicht
 * anfassen und daher auch keine Stereotyp-Annotation anbringen. Solche
 * Klassen werden ueber eine {@code @Bean}-Methode in einer
 * {@code @Configuration}-Klasse registriert — siehe {@link TrainerConfig}.
 *
 * <p><strong>Diese Klasse nicht veraendern.</strong></p>
 */
public class TrainerDirectory {

    private final Map<String, Trainer> trainersByEmail = new HashMap<>();

    public void register(Trainer trainer) {
        trainersByEmail.put(trainer.email(), trainer);
    }

    public Optional<Trainer> findByEmail(String email) {
        return Optional.ofNullable(trainersByEmail.get(email));
    }
}
