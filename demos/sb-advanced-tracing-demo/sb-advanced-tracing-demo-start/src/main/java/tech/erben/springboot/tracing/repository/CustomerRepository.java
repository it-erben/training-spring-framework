package tech.erben.springboot.tracing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.erben.springboot.tracing.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
