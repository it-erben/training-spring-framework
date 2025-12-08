package tech.erben.springboot.amqpdemo.service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import tech.erben.springboot.amqpdemo.model.ProcessedMessage;

@Service
public class InMemoryDeliveryLog {

    private final List<ProcessedMessage> entries = new CopyOnWriteArrayList<>();

    public ProcessedMessage record(String queue, String note, Object payload) {
        ProcessedMessage message = new ProcessedMessage(
            queue,
            note,
            payload,
            Instant.now()
        );
        entries.add(message);
        return message;
    }

    public List<ProcessedMessage> all() {
        return List.copyOf(entries);
    }

    public List<ProcessedMessage> forQueue(String queue) {
        return entries.stream().filter(entry -> entry.queue().equals(queue)).toList();
    }

    public void clear() {
        entries.clear();
    }
}
