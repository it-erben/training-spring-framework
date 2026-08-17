package tech.erben.springboot.basics.web;

/**
 * Fachliche Ausnahme: Zu einer ISBN gibt es kein Buch. Der Service kennt
 * kein HTTP — in der Live-Demo entsteht ein Exception-Handler, der diese
 * Ausnahme in einen 404-Status übersetzt.
 */
// TODO: Modul 02 — Schritt 4: im RestExceptionHandler in Status 404 übersetzen — ohne Handler wird daraus ein 500
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
