package tech.erben.springboot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class QuoteClient {

    private final RestClient restClient;

    public QuoteClient(
        @Value("${quote.api.base-url}") String baseUrl,
        RestClient.Builder builder
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public Quote fetchQuote() {
        return restClient.get().uri("/quote").retrieve().body(Quote.class);
    }
}
