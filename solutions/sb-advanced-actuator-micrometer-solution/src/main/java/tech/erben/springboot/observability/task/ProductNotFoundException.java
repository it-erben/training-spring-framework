package tech.erben.springboot.observability.task;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Product with id " + id + " was not found.");
    }
}
