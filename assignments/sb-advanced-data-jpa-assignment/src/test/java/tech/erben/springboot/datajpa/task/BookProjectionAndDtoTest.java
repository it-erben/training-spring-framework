package tech.erben.springboot.datajpa.task;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
class BookProjectionAndDtoTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Aufgabe 3: findAllBy liefert BookIdentity-Projektionen")
    void findAllByReturnsIdentityProjections() {
        bookRepository.save(new Book("Effective Java", "Joshua Bloch", "9780134685991"));
        bookRepository.save(new Book("Clean Code", "Robert C. Martin", "9780132350884"));

        List<BookIdentity> identities = bookRepository.findAllBy();

        assertThat(identities)
                .extracting(BookIdentity::getTitle, BookIdentity::getIsbn)
                .containsExactlyInAnyOrder(
                        tuple("Effective Java", "9780134685991"),
                        tuple("Clean Code", "9780132350884")
                );
    }

    @Test
    @DisplayName("Aufgabe 3: findAllBookAuthorDTOs befüllt title und authorName per JPQL")
    void findAllBookAuthorDtosMapsTitleAndAuthor() {
        bookRepository.save(new Book("Effective Java", "Joshua Bloch", "9780134685991"));

        List<BookAuthorDTO> dtos = bookRepository.findAllBookAuthorDTOs();

        assertThat(dtos)
                .extracting(BookAuthorDTO::getTitle, BookAuthorDTO::getAuthorName)
                .containsExactly(tuple("Effective Java", "Joshua Bloch"));
    }
}
