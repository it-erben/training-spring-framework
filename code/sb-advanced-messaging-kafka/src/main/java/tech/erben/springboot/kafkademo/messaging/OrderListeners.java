package tech.erben.springboot.kafkademo.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tech.erben.springboot.kafkademo.config.KafkaTopologyConfig;
import tech.erben.springboot.kafkademo.model.BroadcastMessage;
import tech.erben.springboot.kafkademo.model.OrderMessage;
import tech.erben.springboot.kafkademo.service.InMemoryDeliveryLog;

@Component
public class OrderListeners {

    private final InMemoryDeliveryLog deliveryLog;

    public OrderListeners(InMemoryDeliveryLog deliveryLog) {
        this.deliveryLog = deliveryLog;
    }

    @KafkaListener(
        topics = KafkaTopologyConfig.STANDARD_TOPIC,
        groupId = KafkaTopologyConfig.STANDARD_GROUP
    )
    public void handleStandard(
        OrderMessage message,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        deliveryLog.record(
            topic,
            "standard order (container commits offsets in batch)",
            message
        );
    }

    @KafkaListener(
        topics = KafkaTopologyConfig.PRIORITY_TOPIC,
        groupId = KafkaTopologyConfig.PRIORITY_GROUP,
        containerFactory = "manualAckKafkaListenerContainerFactory"
    )
    public void handlePriority(OrderMessage message, Acknowledgment acknowledgment) {
        deliveryLog.record(
            KafkaTopologyConfig.PRIORITY_TOPIC,
            "priority order (manual acknowledge)",
            message
        );
        if (message.simulateError()) {
            deliveryLog.record(
                KafkaTopologyConfig.PRIORITY_TOPIC,
                "simulateError=true -> throw exception to dead-letter",
                message
            );
            throw new IllegalStateException("Simulated failure to trigger DLT");
        }
        acknowledgment.acknowledge();
    }

    @KafkaListener(
        topics = KafkaTopologyConfig.AUDIT_TOPIC,
        groupId = KafkaTopologyConfig.AUDIT_GROUP
    )
    public void handleAudit(OrderMessage message) {
        deliveryLog.record(
            KafkaTopologyConfig.AUDIT_TOPIC,
            "audit tap (copy of every order)",
            message
        );
    }

    @KafkaListener(
        topics = KafkaTopologyConfig.DEAD_LETTER_TOPIC,
        groupId = KafkaTopologyConfig.DLT_GROUP
    )
    public void handleDeadLetter(
        OrderMessage message,
        @Header(
            value = KafkaHeaders.DLT_ORIGINAL_TOPIC,
            required = false
        ) String originalTopic,
        @Header(
            value = KafkaHeaders.DLT_ORIGINAL_PARTITION,
            required = false
        ) Integer partition
    ) {
        String note =
            "dead-letter from %s partition %s".formatted(
                    originalTopic != null ? originalTopic : "?",
                    partition != null ? partition : "?"
                );
        deliveryLog.record(KafkaTopologyConfig.DEAD_LETTER_TOPIC, note, message);
    }

    @KafkaListener(
        topics = KafkaTopologyConfig.BROADCAST_TOPIC,
        groupId = KafkaTopologyConfig.EMAIL_GROUP
    )
    public void handleEmailNotification(BroadcastMessage message) {
        deliveryLog.record(
            KafkaTopologyConfig.BROADCAST_TOPIC,
            "broadcast -> email subscriber",
            message
        );
    }

    @KafkaListener(
        topics = KafkaTopologyConfig.BROADCAST_TOPIC,
        groupId = KafkaTopologyConfig.SMS_GROUP
    )
    public void handleSmsNotification(BroadcastMessage message) {
        deliveryLog.record(
            KafkaTopologyConfig.BROADCAST_TOPIC,
            "broadcast -> sms subscriber",
            message
        );
    }
}
