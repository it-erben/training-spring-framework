package tech.erben.springboot.datajpa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByLastName(String lastName);

    @Query("select c from Customer c where c.firstName = ?1")
    Customer findByFirstNameCustom(String firstName);

    @EntityGraph(attributePaths = { "orders" })
    List<Customer> findWithOrdersByLastName(String lastName);

    List<CustomerNameOnly> findProjectionsByLastName(String lastName);

    @Query(
        "SELECT new tech.erben.springboot.datajpa.CustomerDTO(concat(c.firstName, ' ', c.lastName), c.address.city) FROM Customer c WHERE c.address.city = :city"
    )
    List<CustomerDTO> findCustomerDtosByCity(String city);

    @Modifying
    @Query("UPDATE Customer c SET c.firstName = :newName WHERE c.lastName = :lastName")
    void updateFirstNameByLastName(String lastName, String newName);
}
