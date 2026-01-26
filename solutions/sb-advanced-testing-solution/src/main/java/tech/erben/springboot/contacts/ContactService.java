package tech.erben.springboot.contacts;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public Contact create(ContactCreateDto dto) {
        Contact contact = new Contact(
                dto.firstName(),
                dto.lastName(),
                dto.email()
        );
        return contactRepository.save(contact);
    }

    public List<Contact> searchByLastNamePrefix(String prefix) {
        return contactRepository.findByLastNameStartingWithIgnoreCase(prefix);
    }
}
