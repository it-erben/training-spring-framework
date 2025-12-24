package tech.erben.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EntityScan("tech.erben.springboot")
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testCreateUser() {
        UserEntity newUser = new UserEntity();
        newUser.setName("John Doe");
        newUser.setEmail("john.doe@example.com");

        testEntityManager.persist(newUser);
        testEntityManager.flush();

        List<UserEntity> users = userRepository.findAll();
        assertThat(users.size()).isEqualTo(1);
        assertThat(users.getFirst().getName()).isEqualTo(newUser.getName());
        assertThat(users.getFirst().getEmail()).isEqualTo(newUser.getEmail());
    }
}
