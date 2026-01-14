package tech.erben.springboot.resilience4jdemo.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.erben.springboot.resilience4jdemo.client.ExternalPricingClient;
import tech.erben.springboot.resilience4jdemo.model.Product;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Product service demonstrating Bulkhead pattern.
 * Limits concurrent access to protect against resource exhaustion.
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ExternalPricingClient pricingClient;

    // Simulated product database
    private final Map<String, Product> products = new ConcurrentHashMap<>();

    public ProductService(ExternalPricingClient pricingClient) {
        this.pricingClient = pricingClient;

        // Initialize with sample products (no prices yet)
        products.put("1", new Product("1", "Laptop", null, null));
        products.put("2", new Product("2", "Smartphone", null, null));
        products.put("3", new Product("3", "Tablet", null, null));
        products.put("4", new Product("4", "Headphones", null, null));
        products.put("5", new Product("5", "Smartwatch", null, null));
    }

    /**
     * Gets a product with its current price.
     * Protected by Bulkhead to limit concurrent calls.
     */
    @Bulkhead(name = "productService", fallbackMethod = "getProductFallback")
    public Product getProduct(String productId) {
        log.info("Getting product: {}", productId);

        Product product = products.get(productId);
        if (product == null) {
            throw new ProductNotFoundException("Product not found: " + productId);
        }

        // Fetch current price from external service
        BigDecimal price = pricingClient.getPrice(productId);
        String source = price.equals(new BigDecimal("99.99")) ? "fallback" : "external";

        return product.withPrice(price, source);
    }

    /**
     * Async version - gets product with price using TimeLimiter.
     */
    @Bulkhead(name = "productService", type = Bulkhead.Type.SEMAPHORE)
    public CompletableFuture<Product> getProductAsync(String productId) {
        log.info("Getting product async: {}", productId);

        Product product = products.get(productId);
        if (product == null) {
            return CompletableFuture.failedFuture(
                    new ProductNotFoundException("Product not found: " + productId));
        }

        return pricingClient.getPriceAsync(productId)
                .thenApply(price -> {
                    String source = price.equals(new BigDecimal("99.99")) ? "fallback" : "external";
                    return product.withPrice(price, source);
                });
    }

    /**
     * Fallback when bulkhead is full.
     */
    public Product getProductFallback(String productId, Throwable throwable) {
        log.warn("Bulkhead fallback for product {}. Reason: {}", productId, throwable.getMessage());

        Product product = products.get(productId);
        if (product == null) {
            throw new ProductNotFoundException("Product not found: " + productId);
        }

        return product.withPrice(new BigDecimal("0.00"), "unavailable");
    }

    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(String message) {
            super(message);
        }
    }
}
