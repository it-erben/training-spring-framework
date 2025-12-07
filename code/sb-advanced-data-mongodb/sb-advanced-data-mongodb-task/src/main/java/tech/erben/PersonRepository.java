package tech.erben;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PersonRepository extends MongoRepository<Person, String> {
    List<Person> findPersonsByBirthdayAfter(LocalDate cutoff);
}
