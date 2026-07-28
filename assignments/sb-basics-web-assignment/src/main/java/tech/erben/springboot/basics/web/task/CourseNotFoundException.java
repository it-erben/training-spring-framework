package tech.erben.springboot.basics.web.task;

/**
 * Fachliche Ausnahme: Zu einem Kurscode gibt es keinen Kurs. Der Service
 * kennt kein HTTP — erst euer {@code RestExceptionHandler} (Aufgabe 2)
 * uebersetzt diese Ausnahme in einen 404-Status. Ohne ihn wird daraus
 * ein 500.
 */
public class CourseNotFoundException extends RuntimeException {

    private final String code;

    public CourseNotFoundException(String code) {
        super("Kein Kurs mit Code %s gefunden".formatted(code));
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
