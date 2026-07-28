package tech.erben.springboot.basics.testing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring-Data-Repository wie in Modul 03: kein Implementierungsdetail,
 * CRUD kommt aus {@link JpaRepository}, die Suche nach der ISBN ist eine
 * Derived Query. Im Unit-Test wird genau dieses Interface gemockt — dass
 * es zur Laufzeit ein Datenbank-Proxy ist, spielt dort keine Rolle.
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    /** Derived Query: {@code where isbn = ?}. */
    Optional<Book> findByIsbn(String isbn);
}
