package tech.erben.springboot.basics.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Zentrale Fehlerbehandlung fuer alle Controller. Ohne diese Klasse wuerde
 * eine {@link BookNotFoundException} als 500 beim Client ankommen — hier
 * wird sie in den fachlich richtigen Status 404 uebersetzt.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    /** Unbekannte ISBN → 404 mit einer kurzen Fehlermeldung als JSON. */
    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleBookNotFound(BookNotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }

    /**
     * Fehlgeschlagene Bean-Validation → 400. Den Status wuerde Spring auch
     * ohne diesen Handler liefern — der Mehrwert ist der lesbare Body:
     * eine Map von Feldname auf Fehlermeldung statt einer generischen
     * Fehlerseite.
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
