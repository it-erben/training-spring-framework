package tech.erben.springboot.basics.web.task;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loesung Aufgaben 2 und 4: Zentrale Fehlerbehandlung fuer alle Controller.
 * Ohne diese Klasse wuerde eine {@link CourseNotFoundException} als 500
 * beim Client ankommen — hier wird sie in den fachlich richtigen Status
 * 404 uebersetzt.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    /** Aufgabe 2: Unbekannter Kurscode → 404 mit einer kurzen Fehlermeldung als JSON. */
    @ExceptionHandler(CourseNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleCourseNotFound(CourseNotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }

    /**
     * Aufgabe 4: Fehlgeschlagene Bean-Validation → 400. Den Status wuerde
     * Spring auch ohne diesen Handler liefern — der Mehrwert ist der
     * lesbare Body: eine Map von Feldname auf Fehlermeldung statt einer
     * generischen Fehlerseite.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return errors;
    }
}
