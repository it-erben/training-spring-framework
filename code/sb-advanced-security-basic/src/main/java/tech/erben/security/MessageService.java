package tech.erben.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class MessageService {

    @PreAuthorize("authenticated")
    public String findMessage() {
        return "Hello User!";
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String findSecretMessage() {
        return "Hello Admin!";
    }
}
