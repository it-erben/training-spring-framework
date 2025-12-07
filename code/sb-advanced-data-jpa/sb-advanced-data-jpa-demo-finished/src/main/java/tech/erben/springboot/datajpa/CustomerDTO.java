package tech.erben.springboot.datajpa;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerDTO {

    private String fullName;
    private String city;
}
