package tech.erben.springboot.resilience4jdemo;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.erben.springboot.resilience4jdemo.client.ExternalPricingClient;
import tech.erben.springboot.resilience4jdemo.service.ProductService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class Resilience4jDemoApplicationTests {

    @Autowired
    private ProductService productService;

    @Autowired
    private ExternalPricingClient pricingClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    @Test
    void contextLoads() {
        assertThat(productService).isNotNull();
        assertThat(pricingClient).isNotNull();
    }

    @Test
    void circuitBreakerRegistryIsConfigured() {
        assertThat(circuitBreakerRegistry.find("pricingService")).isPresent();
    }

    @Test
    void rateLimiterRegistryIsConfigured() {
        assertThat(rateLimiterRegistry.find("productApi")).isPresent();
    }
}
