package tech.erben.springboot.basics.core.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Trainerverzeichnis — steht hier stellvertretend für eine Klasse aus
 * einer fremden Bibliothek: Ihr könnt (bzw. dürft) den Quelltext nicht
 * anfassen und daher auch keine Stereotyp-Annotation anbringen. Solche
 * Klassen werden über eine {@code @Bean}-Methode in einer
 * {@code @Configuration}-Klasse registriert. Siehe {@link TrainerConfig}.
 *
 * <p><strong>Diese Klasse nicht verändern.</strong></p>
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
