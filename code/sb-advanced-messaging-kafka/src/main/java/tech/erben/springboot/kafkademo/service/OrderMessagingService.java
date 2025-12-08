package tech.erben.springboot.kafkademo.service;

import java.time.Instant;
import java.util.UUID;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tech.erben.springboot.kafkademo.config.KafkaTopologyConfig;
import tech.erben.springboot.kafkademo.model.BroadcastMessage;
import tech.erben.springboot.kafkademo.model.OrderMessage;
import tech.erben.springboot.kafkademo.web.AnnouncementRequest;
import tech.erben.springboot.kafkademo.web.OrderRequest;

@Service
public class OrderMessagingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderMessagingService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderMessage sendOrder(OrderRequest request) {
        Instant now = Instant.now();
        String topic = request.priority()
            ? KafkaTopologyConfig.PRIORITY_TOPIC
            : KafkaTopologyConfig.STANDARD_TOPIC;
        OrderMessage message = new OrderMessage(
            UUID.randomUUID().toString(),
            request.customer(),
            request.item(),
            request.quantity(),
            request.priority(),
            request.simulateError(),
            topic,
            now
        );
        kafkaTemplate.send(topic, request.customer(), message);
        kafkaTemplate.send(KafkaTopologyConfig.AUDIT_TOPIC, request.customer(), message);
        return message;
    }

    public BroadcastMessage broadcast(AnnouncementRequest request) {
        BroadcastMessage message = new BroadcastMessage(request.message(), Instant.now());
        kafkaTemplate.send(KafkaTopologyConfig.BROADCAST_TOPIC, message);
        return message;
    }
}
