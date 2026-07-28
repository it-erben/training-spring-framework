package tech.erben.springboot.basics.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Kein einziges Implementierungsdetail: Spring Data JPA erzeugt zur
 * Laufzeit ein Proxy-Objekt, das CRUD ({@code save}, {@code findAll},
 * {@code deleteAll}, ...) aus {@link JpaRepository} erbt und die Derived
 * Queries aus den Methodennamen ableitet. Wo der Name nicht mehr lesbar
 * waere, hilft {@link Query} mit JPQL.
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    /** Derived Query: {@code where isbn = ?}. */
    Optional<Book> findByIsbn(String isbn);

    /** Derived Query: {@code where lower(title) like lower('%fragment%')}. */
    List<Book> findByTitleContainingIgnoreCase(String fragment);

    /** Derived Query: {@code where net_price < ?}. */
    List<Book> findByNetPriceLessThan(BigDecimal limit);

    /**
     * JPQL statt Methodennamen-Magie: {@code b.author.name} navigiert ueber
     * die Beziehung — Hibernate macht daraus einen Join auf {@code author}.
     */
    @Query("select b from Book b where b.author.name = :name")
    List<Book> findByAuthorName(@Param("name") String name);
}
