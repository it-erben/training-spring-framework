package tech.erben.springboot.webdemo.service;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("Book with id %d not found".formatted(id));
    }
}
