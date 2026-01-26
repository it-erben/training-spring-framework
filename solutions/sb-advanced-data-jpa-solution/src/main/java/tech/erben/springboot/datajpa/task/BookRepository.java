package tech.erben.springboot.datajpa.task;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByAuthor(String author);
    List<Book> findByTitleContaining(String keyword);

    @Query("select count(b) from Book b where b.author = ?1")
    long countBooksByAuthor(String author);

    @EntityGraph(attributePaths = { "reviews" })
    List<Book> findWithReviewsByTitle(String title);

    List<BookIdentity> findProjectionsByAuthor(String author);

    @Query(
        "SELECT new tech.erben.springboot.datajpa.task.BookAuthorDTO(b.title, b.author) FROM Book b WHERE b.publisherInfo.name = :publisherName"
    )
    List<BookAuthorDTO> findDtosByPublisher(String publisherName);

}
