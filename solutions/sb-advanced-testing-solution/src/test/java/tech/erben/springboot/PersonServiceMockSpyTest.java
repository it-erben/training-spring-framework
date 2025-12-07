package tech.erben.springboot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpringBootTest
class PersonServiceMockSpyTest {

    @Autowired
    PersonService personService;

    @MockBean
    PersonRepository personRepository;

    @SpyBean
    PersonLoggingService personAuditService;

    @Test
    void usesRepositoryAndAuditService() {
        when(personRepository.save(any(Person.class)))
            .thenAnswer(invocation -> {
                Person p = invocation.getArgument(0);
                p.setId(42L);
                return p;
            });

        Person result = personService.createPerson("Bob", 25);

        assertThat(result.getId()).isEqualTo(42L);
        verify(personRepository).save(any(Person.class));
        verify(personAuditService).recordCreated(any(Person.class));
    }
}
