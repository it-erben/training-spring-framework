package tech.erben.springboot.basics.testing;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Zentrale Fehlerbehandlung wie in Modul 02: übersetzt fachliche
 * Ausnahmen in die passenden Statuscodes. Im Slice-Test lädt
 * {@code @WebMvcTest} diese Advice-Klasse automatisch mit — deshalb
 * lässt sich der 404-Fall dort ohne komplette Anwendung prüfen.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    /** Unbekannte ISBN → 404 mit einer kurzen Fehlermeldung als JSON. */
    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleBookNotFound(BookNotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }

    /** Fehlgeschlagene Bean-Validation → 400 mit Feldname und Meldung. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return errors;
    }
}
