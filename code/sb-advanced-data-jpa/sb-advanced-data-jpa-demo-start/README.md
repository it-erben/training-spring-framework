# Spring Data JPA - Deep Dive Demo

Ziel: Verstehen, wie Spring Data JPA funktioniert, wie Repositories instanziiert werden und wie fortgeschrittene Features wie EntityGraphs, Projections, Auditing und Embedded Types genutzt werden.

Setup: Start im Projekt `sb-advanced-data-jpa-demo-start`.

## 1. Dependencies & Setup

Zeige die `pom.xml`.

- `spring-boot-starter-data-jpa`: Enthält Hibernate, Spring ORM, Spring Data JPA.
- `h2`: In-Memory Datenbank.

Erstelle die Main-Application-Klasse und aktiviere Auditing:

```java
@SpringBootApplication
@EnableJpaAuditing // WICHTIG für Auditing
public class DataJpaDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataJpaDemoApplication.class, args);
    }
}
```

## 2. Embedded Type & Auditing

Wir wollen eine Adresse als wiederverwendbaren Typen (`@Embeddable`) und automatische Zeitstempel (`@CreatedDate`, `@LastModifiedDate`).

Erstelle `Address.java`:

```java
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String city;
    private String street;
}
```

Erstelle `Order.java` (für spätere EntityGraph Demo):

```java
@Entity
@Table(name = "customer_orders") // Order ist ein reserviertes SQL Keyword
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
```

Update `Customer.java`:

```java
@Entity
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) // Trigger für Auditing events
public class Customer {
    @Id
    @GeneratedValue
    private Long id;
    private String firstName;
    private String lastName;

    @Embedded
    private Address address;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private List<Order> orders = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public Customer(String firstName, String lastName, Address address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }
}
```

## 3. DTOs & Projections

Oft wollen wir nicht die ganze Entity laden.

**Interface Projection:** `CustomerNameOnly.java`

```java
public interface CustomerNameOnly {
    String getFirstName();
    String getLastName();
    // Spring Data JPA generiert zur Laufzeit einen Proxy, der nur diese Felder lädt
}
```

**Class DTO:** `CustomerDTO.java`

```java
@Data
@AllArgsConstructor
public class CustomerDTO {
    private String fullName;
    private String city;
}
```

## 4. Repository Features

Update `CustomerRepository.java`:

```java
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 1. EntityGraph: Lädt 'orders' gleich mit (Eager fetching statt Lazy)
    // Löst das N+1 Problem
    @EntityGraph(attributePaths = {"orders"})
    List<Customer> findWithOrdersByLastName(String lastName);

    // 2. Projections
    List<CustomerNameOnly> findProjectionsByLastName(String lastName);

    // 3. DTO mit JPQL Constructor Expression
    @Query("SELECT new tech.erben.springboot.datajpa.CustomerDTO(concat(c.firstName, ' ', c.lastName), c.address.city) FROM Customer c WHERE c.address.city = :city")
    List<CustomerDTO> findCustomerDtosByCity(String city);

    // 4. Modifying Queries (Update/Delete)
    @Modifying
    @Query("UPDATE Customer c SET c.firstName = :newName WHERE c.lastName = :lastName")
    void updateFirstNameByLastName(String lastName, String newName);
}
```

## 5. Anwendung im Runner

Update `DataJpaDemoApplication`:

```java
@Bean
public CommandLineRunner demo(CustomerRepository repository) {
    return (args) -> {
        // Setup Data
        Customer c1 = new Customer("Jack", "Bauer", new Address("Los Angeles", "Sunset Blvd"));
        c1.getOrders().add(new Order("Pistol"));
        c1.getOrders().add(new Order("Phone"));
        repository.save(c1);

        Customer c2 = new Customer("Chloe", "O'Brian", new Address("New York", "5th Ave"));
        repository.save(c2);

        // 1. Entity Graph
        System.out.println("--- EntityGraph ---");
        List<Customer> customers = repository.findWithOrdersByLastName("Bauer");
        customers.forEach(c -> System.out.println(c.getFirstName() + " has " + c.getOrders().size() + " orders"));

        // 2. Projections
        System.out.println("--- Projection ---");
        repository.findProjectionsByLastName("Bauer")
                .forEach(p -> System.out.println(p.getFirstName() + " " + p.getLastName()));

        // 3. DTOs
        System.out.println("--- DTO via JPQL ---");
        repository.findCustomerDtosByCity("Los Angeles")
                .forEach(dto -> System.out.println(dto));

        // 4. Auditing
        System.out.println("--- Auditing ---");
        System.out.println("Created Date: " + customers.get(0).getCreatedDate());

        // 5. Modifying (needs Transaction)
        // Hinweis: Normalerweise @Transactional am Service, hier Workaround oder TransactionTemplate
    };
}
```

Um `@Modifying` im Runner auszuführen, müsste man eigentlich eine Transaktion öffnen. Für die Demo reicht der Hinweis oder das Hinzufügen von `@Transactional` an der Runner-Methode (funktioniert in Tests, im Runner teils tricky, besser in Service auslagern).

**Zusammenfassung:**

- **EntityGraph**: Performance-Optimierung.
- **Projections/DTOs**: Daten-Reduktion.
- **Embedded**: Strukturierung.
- **Auditing**: Automatisierung von Metadaten.
- **Modifying**: Bulk Updates.
