package tech.erben.springboot.datajpa.task;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookAuthorDTO {

    private String title;
    private String authorName;
}
