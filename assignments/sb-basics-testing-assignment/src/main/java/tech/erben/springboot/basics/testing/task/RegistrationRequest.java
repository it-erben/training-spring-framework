package tech.erben.springboot.basics.testing.task;

/**
 * Request-Body der Anmeldung: {@code {"email": "..."}}.
 */
public record RegistrationRequest(String email) {
}
