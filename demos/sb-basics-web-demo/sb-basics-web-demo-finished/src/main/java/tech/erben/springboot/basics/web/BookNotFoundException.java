package tech.erben.springboot.basics.web;

/**
 * Fachliche Ausnahme: Zu einer ISBN gibt es kein Buch. Der Service kennt
 * kein HTTP — erst der {@link RestExceptionHandler} übersetzt diese
 * Ausnahme in einen 404-Status.
 */
public class BookNotFoundException extends RuntimeException {

    private final String isbn;

    public BookNotFoundException(String isbn) {
        super("Kein Buch mit ISBN %s gefunden".formatted(isbn));
        this.isbn = isbn;
    }

    public String getIsbn() {
        return isbn;
    }
}
