package tech.erben.springboot.observability.task;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repository;
    private final MeterRegistry meterRegistry;

    public ProductService(ProductRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.meterRegistry = meterRegistry;
        // TODO Micrometer-Metriken initialisieren (Counter, Timer, Gauge)
    }

    public ProductResponse create(ProductCreateRequest request) {
        if (repository.existsByNameIgnoreCase(request.getName())) {
            // TODO rejected Counter erhoehen
            throw new IllegalArgumentException(
                    "A product with the name '" + request.getName() + "' already exists."
            );
        }

        Product saved = repository.save(
                new Product(request.getName(), request.getPrice(), request.getInventory())
        );

        // TODO success Counter erhoehen
        return ProductResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(String query) {
        // TODO Ausfuehrungszeit mit einem Timer messen
        List<Product> products;
        if (query == null || query.isBlank()) {
            products = repository.findAll();
        } else {
            products = repository.findByNameContainingIgnoreCase(query);
        }

        return products.stream().map(ProductResponse::from).toList();
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
        return ProductResponse.from(saved);
    }

    public void delete(Long id) {
        Product product = repository
                .findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        repository.delete(product);
        // TODO optional: delete Counter erhoehen
    }
}
