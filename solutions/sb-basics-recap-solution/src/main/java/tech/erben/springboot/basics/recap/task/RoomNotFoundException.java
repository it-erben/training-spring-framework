package tech.erben.springboot.basics.recap.task;

/**
 * Fachliche Ausnahme für eine unbekannte Raum-ID. Der
 * {@link RestExceptionHandler} übersetzt sie in 404.
 */
public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(Long id) {
        super("Kein Raum mit ID " + id + " gefunden");
    }
}
