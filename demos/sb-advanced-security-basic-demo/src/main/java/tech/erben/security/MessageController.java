package tech.erben.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    private final MessageService messages;

    public MessageController(MessageService messages) {
        this.messages = messages;
    }

    @GetMapping("/message")
    public String message() {
        return this.messages.findMessage();
    }

    @GetMapping("/secret")
    public String secretMessage() {
        return this.messages.findSecretMessage();
    }
}
