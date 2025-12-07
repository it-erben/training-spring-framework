package tech.erben.springboot.datajpa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_orders")
@Data
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    private String product;

    public Order(String product) {
        this.product = product;
    }
}
