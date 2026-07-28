package tech.erben.springboot.basics.core.task;

/**
 * Ein Trainer, der Kurse haelt. Die E-Mail-Adresse dient als eindeutiger
 * Schluessel im {@link TrainerDirectory}.
 */
public record Trainer(String name, String email) {
}
