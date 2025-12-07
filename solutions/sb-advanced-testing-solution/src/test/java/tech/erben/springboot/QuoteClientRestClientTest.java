package tech.erben.springboot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

@RestClientTest(QuoteClient.class)
@org.springframework.test.context.TestPropertySource(
    properties = "quote.api.base-url=http://localhost:8089"
)
class QuoteClientRestClientTest {

    @Autowired
    QuoteClient quoteClient;

    @Autowired
    MockRestServiceServer server;

    @Test
    void fetchesQuoteFromApi() {
        server
            .expect(requestTo("http://localhost:8089/quote"))
            .andRespond(
                withSuccess(
                    """
                {"id":"1","text":"Test quote"}
                """,
                    MediaType.APPLICATION_JSON
                )
            );

        Quote quote = quoteClient.fetchQuote();

        assertThat(quote.text()).isEqualTo("Test quote");
    }
}
