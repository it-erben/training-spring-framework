package tech.erben.springboot.webdemo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient bookRestClient(
        RestClient.Builder builder,
        @Value("${demo.remote.base-url:http://localhost:8080}") String baseUrl
    ) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    BookHttpApi bookHttpApi(RestClient bookRestClient) {
        RestClientAdapter adapter = RestClientAdapter.create(bookRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(adapter)
            .build();
        return factory.createClient(BookHttpApi.class);
    }

    @Bean
    BookClient bookClient(RestClient bookRestClient, BookHttpApi bookHttpApi) {
        return new BookClient(bookRestClient, bookHttpApi);
    }

    @Bean
    @ConditionalOnProperty(name = "demo.restclient.log-sample", havingValue = "true")
    ApplicationRunner restClientSample(BookClient bookClient) {
        return args -> bookClient.logSampleCalls();
    }
}
