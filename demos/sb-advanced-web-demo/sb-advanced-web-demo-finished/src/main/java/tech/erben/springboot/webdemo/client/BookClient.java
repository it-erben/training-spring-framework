package tech.erben.springboot.webdemo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import tech.erben.springboot.webdemo.api.BookResponse;

import java.util.List;

public class BookClient {

    private static final Logger log = LoggerFactory.getLogger(BookClient.class);

    private final RestClient restClient;
    private final BookHttpApi bookHttpApi;

    public BookClient(RestClient restClient, BookHttpApi bookHttpApi) {
        this.restClient = restClient;
        this.bookHttpApi = bookHttpApi;
    }

    public void logSampleCalls() {
        try {
            List<BookResponse> responses = restClient
                .get()
                .uri("/api/v1/books")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
            log.info(
                "RestClient fetched {} book(s)",
                responses == null ? 0 : responses.size()
            );
        } catch (Exception ex) {
            log.warn(
                "RestClient sample call failed (server might not be reachable): {}",
                ex.getMessage()
            );
        }

        try {
            BookResponse response = bookHttpApi.findById(1L);
            log.info("Declarative client book 1 title: {}", response.title());
        } catch (Exception ex) {
            log.warn("Declarative HTTP interface call failed: {}", ex.getMessage());
        }
    }
}
