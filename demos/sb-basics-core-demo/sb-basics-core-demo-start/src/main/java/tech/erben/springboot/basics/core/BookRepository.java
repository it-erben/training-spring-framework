package tech.erben.springboot.basics.core;

import java.util.List;
import java.util.Optional;

/**
 * Zugriff auf den Buchbestand. Der {@link BookService} hängt nur von diesem
 * Interface ab — welche Implementierung der Container injiziert, ist ihm egal.
 */
public interface BookRepository {

    List<Book> findAll();

    Optional<Book> findByIsbn(String isbn);
}
