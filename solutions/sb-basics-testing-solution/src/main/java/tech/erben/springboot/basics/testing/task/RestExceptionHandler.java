package tech.erben.springboot.basics.testing.task;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Zentrale Fehlerbehandlung wie in Modul 02: Ohne diese Klasse käme
 * eine {@link CourseNotFoundException} als 500 beim Client an — hier
 * wird sie in den fachlich richtigen Status 404 übersetzt.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(CourseNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleCourseNotFound(CourseNotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }
}
