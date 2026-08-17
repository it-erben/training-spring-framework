package tech.erben.springboot.basics.core.task;

/**
 * Ein Trainer, der Kurse hält. Die E-Mail-Adresse dient als eindeutiger
 * Schlüssel im {@link TrainerDirectory}.
 */
public record Trainer(String name, String email) {
}
