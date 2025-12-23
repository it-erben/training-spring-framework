package tech.erben.springboot.kafkademo.service;

import org.springframework.stereotype.Service;
import tech.erben.springboot.kafkademo.model.ProcessedMessage;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class InMemoryDeliveryLog {

    private final List<ProcessedMessage> entries = new CopyOnWriteArrayList<>();

    public ProcessedMessage record(String topic, String note, Object payload) {
        ProcessedMessage message = new ProcessedMessage(
            topic,
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

    public List<ProcessedMessage> forTopic(String topic) {
        return entries.stream().filter(entry -> entry.topic().equals(topic)).toList();
    }

    public void clear() {
        entries.clear();
    }
}
