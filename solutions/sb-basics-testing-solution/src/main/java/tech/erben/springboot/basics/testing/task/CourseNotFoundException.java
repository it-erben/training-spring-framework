package tech.erben.springboot.basics.testing.task;

/**
 * Fachliche Ausnahme für einen unbekannten Kurscode. Der
 * {@link RestExceptionHandler} übersetzt sie in 404.
 */
public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(String code) {
        super("Kein Kurs mit Code " + code + " gefunden");
    }
}
