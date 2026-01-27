package tech.erben.springboot.observability.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String query);

    boolean existsByNameIgnoreCase(String name);
}
