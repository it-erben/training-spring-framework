package tech.erben.springboot.webdemo.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import tech.erben.springboot.webdemo.api.BookRequest;
import tech.erben.springboot.webdemo.api.BookResponse;

import java.util.List;

@HttpExchange("/api/v1/books")
public interface BookHttpApi {
    @GetExchange("/{id}")
    BookResponse findById(@PathVariable Long id);

    @GetExchange
    List<BookResponse> findAll();

    @PostExchange
    BookResponse create(@RequestBody BookRequest request);
}
