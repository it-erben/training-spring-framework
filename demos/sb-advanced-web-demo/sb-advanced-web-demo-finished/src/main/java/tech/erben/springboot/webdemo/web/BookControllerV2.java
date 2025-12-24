package tech.erben.springboot.webdemo.web;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.erben.springboot.webdemo.api.BookResponse;
import tech.erben.springboot.webdemo.model.Book;
import tech.erben.springboot.webdemo.service.BookService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(
    value = "/api/v2/books",
    produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class BookControllerV2 {

    private final BookService bookService;

    public BookControllerV2(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public CollectionModel<EntityModel<BookResponse>> list() {
        var items = bookService.findAll().stream().map(this::toModel).toList();
        return CollectionModel.of(
            items,
            linkTo(methodOn(BookControllerV2.class).list()).withSelfRel()
        );
    }

    @GetMapping("/{id}")
    public EntityModel<BookResponse> get(@PathVariable Long id) {
        return toModel(bookService.findById(id));
    }

    private EntityModel<BookResponse> toModel(Book book) {
        BookResponse response = new BookResponse(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getCategory(),
            book.getPrice(),
            book.getPublicationYear(),
            book.getOriginCountry(),
            book.getCreatedAt()
        );

        EntityModel<BookResponse> model = EntityModel.of(response);
        Link self = linkTo(methodOn(BookControllerV2.class).get(book.getId()))
            .withSelfRel();
        model.add(self);
        model.add(linkTo(methodOn(BookControllerV2.class).list()).withRel("collection"));
        model.add(Link.of("/api/v1/books/" + book.getId()).withRel("v1"));
        return model;
    }
}
