package tech.erben.springboot.basics.testing.task;

/**
 * Request-Body der Anmeldung: {@code {"email": "..."}}. Ein Record
 * reicht — Jackson liest und schreibt ihn ohne weiteres Zutun.
 */
public record RegistrationRequest(String email) {
}
