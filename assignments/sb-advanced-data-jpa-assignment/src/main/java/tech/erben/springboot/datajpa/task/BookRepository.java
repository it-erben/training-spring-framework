package tech.erben.springboot.datajpa.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByAuthor(String author);
    List<Book> findByTitleContaining(String keyword);

    @Query("select count(b) from Book b where b.author = ?1")
    long countBooksByAuthor(String author);

}
