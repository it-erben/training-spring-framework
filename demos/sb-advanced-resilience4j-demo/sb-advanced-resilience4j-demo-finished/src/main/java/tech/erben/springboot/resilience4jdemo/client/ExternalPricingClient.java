package tech.erben.springboot.resilience4jdemo.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/**
 * Client for the external pricing service.
 * Demonstrates Circuit Breaker, Retry, and Time Limiter patterns.
 */
@Component
public class ExternalPricingClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalPricingClient.class);

    private final RestClient restClient;

    public ExternalPricingClient(@Value("${pricing.service.url:http://localhost:8081}") String pricingServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(pricingServiceUrl)
                .build();
    }

    /**
     * Fetches the price for a product from the external pricing service.
     * This method is protected by Circuit Breaker and Retry patterns.
     *
     * The order of decorators (from outer to inner): Retry -> CircuitBreaker -> TimeLimiter
     * This means: Retry wraps CircuitBreaker, which wraps TimeLimiter
     */
    @CircuitBreaker(name = "pricingService", fallbackMethod = "getDefaultPrice")
    @Retry(name = "pricingService")
    public BigDecimal getPrice(String productId) {
        log.info("Calling external pricing service for product: {}", productId);

        PriceResponse response = restClient.get()
                .uri("/api/prices/{productId}", productId)
                .retrieve()
                .body(PriceResponse.class);

        if (response == null) {
            throw new RuntimeException("Null response from pricing service");
        }

        log.info("Received price {} for product {}", response.price(), productId);
        return response.price();
    }

    /**
     * Async version of getPrice for demonstrating TimeLimiter.
     * TimeLimiter only works with CompletableFuture return types.
     */
    @TimeLimiter(name = "pricingService", fallbackMethod = "getDefaultPriceAsync")
    @CircuitBreaker(name = "pricingService", fallbackMethod = "getDefaultPriceAsync")
    public CompletableFuture<BigDecimal> getPriceAsync(String productId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Async call to external pricing service for product: {}", productId);

            PriceResponse response = restClient.get()
                    .uri("/api/prices/{productId}", productId)
                    .retrieve()
                    .body(PriceResponse.class);

            if (response == null) {
                throw new RuntimeException("Null response from pricing service");
            }

            return response.price();
        });
    }

    /**
     * Fallback method when the pricing service is unavailable.
     * Returns a default price.
     */
    public BigDecimal getDefaultPrice(String productId, Throwable throwable) {
        log.warn("Fallback triggered for product {}. Reason: {}", productId, throwable.getMessage());
        return new BigDecimal("99.99");
    }

    /**
     * Async fallback method for TimeLimiter/CircuitBreaker.
     */
    public CompletableFuture<BigDecimal> getDefaultPriceAsync(String productId, Throwable throwable) {
        log.warn("Async fallback triggered for product {}. Reason: {}", productId, throwable.getMessage());
        return CompletableFuture.completedFuture(new BigDecimal("99.99"));
    }

    record PriceResponse(BigDecimal price) {}
}
