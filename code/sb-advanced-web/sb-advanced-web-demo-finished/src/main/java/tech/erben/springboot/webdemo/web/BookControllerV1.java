package tech.erben.springboot.webdemo.web;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import tech.erben.springboot.webdemo.api.BookRequest;
import tech.erben.springboot.webdemo.api.BookResponse;
import tech.erben.springboot.webdemo.model.Book;
import tech.erben.springboot.webdemo.service.BookService;
import tech.erben.springboot.webdemo.validation.OnCreate;
import tech.erben.springboot.webdemo.validation.OnUpdate;

@RestController
@RequestMapping(
    value = "/api/v1/books",
    produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class BookControllerV1 {

    private final BookService bookService;

    public BookControllerV1(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookResponse> list() {
        return bookService.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(bookService.findById(id)));
    }

    @GetMapping("/{id}/async")
    public CompletableFuture<BookResponse> getByIdAsync(@PathVariable Long id) {
        return CompletableFuture.completedFuture(toResponse(bookService.findById(id)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookResponse> create(
        @Validated(OnCreate.class) @RequestBody BookRequest request,
        UriComponentsBuilder uriBuilder
    ) {
        Book created = bookService.create(request);
        URI location = uriBuilder
            .path("/api/v1/books/{id}")
            .buildAndExpand(created.getId())
            .toUri();
        return ResponseEntity.created(location).body(toResponse(created));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BookResponse> update(
        @PathVariable Long id,
        @Validated(OnUpdate.class) @RequestBody BookRequest request
    ) {
        Book updated = bookService.update(id, request);
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getCategory(),
            book.getPrice(),
            book.getPublicationYear(),
            book.getOriginCountry(),
            book.getCreatedAt()
        );
    }
}
