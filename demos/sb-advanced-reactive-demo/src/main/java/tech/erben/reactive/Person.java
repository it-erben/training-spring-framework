package tech.erben.reactive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * The Person class represents a person and contains information about their identity and name.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person {

    @Id
    private Long id;

    private String firstName;
    private String lastName;
}
