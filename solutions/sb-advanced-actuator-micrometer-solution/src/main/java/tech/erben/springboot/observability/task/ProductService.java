package tech.erben.springboot.observability.task;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repository;
    private final MeterRegistry meterRegistry;
    private final Counter productsCreatedCounter;
    private final Counter productsRejectedCounter;
    private final Timer productsSearchTimer;
    private final AtomicInteger productsCountGauge;

    public ProductService(ProductRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.meterRegistry = meterRegistry;
        this.productsCreatedCounter = Counter
                .builder("products.created")
                .description("Number of successfully created products")
                .register(meterRegistry);
        this.productsRejectedCounter = Counter
                .builder("products.rejected")
                .description("Number of rejected product create requests")
                .register(meterRegistry);
        this.productsSearchTimer = Timer
                .builder("products.search")
                .description("Time spent searching or listing products")
                .publishPercentileHistogram()
                .register(meterRegistry);
        this.productsCountGauge = meterRegistry.gauge(
                "products.count",
                new AtomicInteger(safeCount(repository.count()))
        );
    }

    public ProductResponse create(ProductCreateRequest request) {
        if (repository.existsByNameIgnoreCase(request.getName())) {
            productsRejectedCounter.increment();
            throw new IllegalArgumentException(
                    "A product with the name '" + request.getName() + "' already exists."
            );
        }

        Product saved = repository.save(
                new Product(request.getName(), request.getPrice(), request.getInventory())
        );

        productsCreatedCounter.increment();
        updateProductsCountGauge();
        return ProductResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(String query) {
        return productsSearchTimer.record(() -> {
            List<Product> products;
            if (query == null || query.isBlank()) {
                products = repository.findAll();
            } else {
                products = repository.findByNameContainingIgnoreCase(query);
            }

            return products.stream().map(ProductResponse::from).toList();
        });
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        Product product = repository
                .findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductResponse.from(product);
    }

    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = repository
                .findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        boolean nameChanged = !product.getName().equalsIgnoreCase(request.getName());
        if (nameChanged && repository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException(
                    "A product with the name '" + request.getName() + "' already exists."
            );
        }

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setInventory(request.getInventory());

        Product saved = repository.save(product);
        updateProductsCountGauge();
        return ProductResponse.from(saved);
    }

    public void delete(Long id) {
        Product product = repository
                .findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        repository.delete(product);
        updateProductsCountGauge();
    }

    private void updateProductsCountGauge() {
        productsCountGauge.set(safeCount(repository.count()));
    }

    private int safeCount(long count) {
        return count > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count;
    }
}
