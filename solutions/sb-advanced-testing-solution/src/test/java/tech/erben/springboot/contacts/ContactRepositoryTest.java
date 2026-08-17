package tech.erben.springboot.contacts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ContactRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private ContactRepository contactRepository;

    @Test
    void findByEmail_returnsContact_whenExists() {
        Contact contact = new Contact("Max", "Mustermann", "max.mustermann@example.com");
        testEntityManager.persist(contact);
        testEntityManager.flush();

        Optional<Contact> found = contactRepository.findByEmail("max.mustermann@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("max.mustermann@example.com");
        assertThat(found.get().getFirstName()).isEqualTo("Max");
        assertThat(found.get().getLastName()).isEqualTo("Mustermann");
    }

    @Test
    void findByEmail_returnsEmpty_whenNotExists() {
        Optional<Contact> found = contactRepository.findByEmail("nicht.vorhanden@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void findByLastNameStartingWithIgnoreCase_returnsMatchingContacts() {
        Contact contact1 = new Contact("Max", "Schmidt", "max.schmidt@example.com");
        Contact contact2 = new Contact("Anna", "Schneider", "anna.schneider@example.com");
        Contact contact3 = new Contact("Peter", "Mueller", "peter.mueller@example.com");
        testEntityManager.persist(contact1);
        testEntityManager.persist(contact2);
        testEntityManager.persist(contact3);
        testEntityManager.flush();

        List<Contact> found = contactRepository.findByLastNameStartingWithIgnoreCase("Sch");

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Contact::getLastName)
                .containsExactlyInAnyOrder("Schmidt", "Schneider");
    }

    @Test
    void findByLastNameStartingWithIgnoreCase_isCaseInsensitive() {
        Contact contact = new Contact("Max", "Schmidt", "max.schmidt@example.com");
        testEntityManager.persist(contact);
        testEntityManager.flush();

        List<Contact> foundLower = contactRepository.findByLastNameStartingWithIgnoreCase("sch");
        List<Contact> foundUpper = contactRepository.findByLastNameStartingWithIgnoreCase("SCH");

        assertThat(foundLower).hasSize(1);
        assertThat(foundUpper).hasSize(1);
    }
}
