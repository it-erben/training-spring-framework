package tech.erben.springboot.datajpa.task;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue
    private Long id;

    private String comment;
    private int rating;

    public Review(String comment, int rating) {
        this.comment = comment;
        this.rating = rating;
    }
}
