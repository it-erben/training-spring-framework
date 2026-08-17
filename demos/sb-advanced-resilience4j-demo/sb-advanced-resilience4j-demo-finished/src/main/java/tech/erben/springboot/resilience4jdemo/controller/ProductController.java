package tech.erben.springboot.resilience4jdemo.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.erben.springboot.resilience4jdemo.model.Product;
import tech.erben.springboot.resilience4jdemo.service.ProductService;

import java.util.concurrent.CompletableFuture;

/**
 * REST Controller demonstrating Rate Limiter pattern.
 * Protects endpoints from being overwhelmed by too many requests.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Protected by Rate Limiter to prevent API abuse.
     */
    @GetMapping("/{id}")
    @RateLimiter(name = "productApi", fallbackMethod = "rateLimitFallback")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        log.info("REST request to get product: {}", id);
        Product product = productService.getProduct(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Async endpoint demonstrating TimeLimiter with CompletableFuture.
     */
    @GetMapping("/{id}/async")
    @RateLimiter(name = "productApi", fallbackMethod = "rateLimitFallbackAsync")
    public CompletableFuture<ResponseEntity<Product>> getProductAsync(@PathVariable String id) {
        log.info("REST request to get product async: {}", id);
        return productService.getProductAsync(id)
                .thenApply(ResponseEntity::ok);
    }

    /**
     * Fallback when rate limit is exceeded.
     */
    public ResponseEntity<Product> rateLimitFallback(String id, Throwable throwable) {
        log.warn("Rate limit exceeded for product request: {}. Reason: {}", id, throwable.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    /**
     * Async fallback for rate limit.
     */
    public CompletableFuture<ResponseEntity<Product>> rateLimitFallbackAsync(String id, Throwable throwable) {
        log.warn("Rate limit exceeded for async product request: {}. Reason: {}", id, throwable.getMessage());
        return CompletableFuture.completedFuture(
                ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build());
    }

    @ExceptionHandler(ProductService.ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductService.ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    record ErrorResponse(String message) {}
}
