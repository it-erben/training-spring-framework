package tech.erben.springboot.tracing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.erben.springboot.tracing.model.CustomerOrder;

import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByCustomerId(Long customerId);
}
