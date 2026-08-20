package tech.erben.springboot.basics.recap.task;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Zentrale Fehlerbehandlung aus Modul 02: Ohne diese Klasse käme eine
 * {@link RoomNotFoundException} als 500 beim Client an.
 */
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(RoomNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleRoomNotFound(RoomNotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }
}
