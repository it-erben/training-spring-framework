package tech.erben.springboot.kafkademo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import tech.erben.springboot.kafkademo.config.KafkaTopologyConfig;
import tech.erben.springboot.kafkademo.model.BroadcastMessage;
import tech.erben.springboot.kafkademo.model.OrderMessage;
import tech.erben.springboot.kafkademo.service.InMemoryDeliveryLog;
import tech.erben.springboot.kafkademo.service.OrderMessagingService;
import tech.erben.springboot.kafkademo.web.AnnouncementRequest;
import tech.erben.springboot.kafkademo.web.OrderRequest;

@Testcontainers
@SpringBootTest
class OrderMessagingIntegrationTest {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
    );

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private OrderMessagingService messagingService;

    @Autowired
    private InMemoryDeliveryLog deliveryLog;

    @BeforeEach
    void clearLog() {
        deliveryLog.clear();
    }

    @Test
    void standardOrderIsConsumedAndAudited() {
        OrderMessage order = messagingService.sendOrder(
            new OrderRequest("standard-user", "book", 1, false, false)
        );

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            assertThat(deliveryLog.forTopic(KafkaTopologyConfig.STANDARD_TOPIC))
                .anySatisfy(entry -> {
                    assertThat(entry.note()).contains("standard order");
                    assertThat(entry.payload()).isEqualTo(order);
                });
        });

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            assertThat(deliveryLog.forTopic(KafkaTopologyConfig.AUDIT_TOPIC))
                .anySatisfy(entry -> {
                    assertThat(entry.note()).contains("audit tap");
                    assertThat(entry.payload()).isEqualTo(order);
                });
        });
    }

    @Test
    void priorityOrderIsManuallyAcknowledgedAndAudited() {
        OrderMessage order = messagingService.sendOrder(
            new OrderRequest("priority-user", "laptop", 1, true, false)
        );

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            assertThat(deliveryLog.forTopic(KafkaTopologyConfig.PRIORITY_TOPIC))
                .anySatisfy(entry -> {
                    assertThat(entry.note()).contains("priority order");
                    assertThat(entry.payload()).isEqualTo(order);
                });
        });

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            assertThat(deliveryLog.forTopic(KafkaTopologyConfig.AUDIT_TOPIC))
                .anySatisfy(entry -> assertThat(entry.payload()).isEqualTo(order));
        });

        assertThat(deliveryLog.forTopic(KafkaTopologyConfig.DEAD_LETTER_TOPIC)).isEmpty();
    }

    @Test
    void failingPriorityOrderIsReroutedToDeadLetterTopic() {
        OrderMessage order = messagingService.sendOrder(
            new OrderRequest("unlucky-user", "fragile", 1, true, true)
        );

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(deliveryLog.forTopic(KafkaTopologyConfig.DEAD_LETTER_TOPIC))
                .anySatisfy(entry -> {
                    assertThat(entry.note())
                        .contains("dead-letter from " + KafkaTopologyConfig.PRIORITY_TOPIC);
                    assertThat(entry.payload()).isEqualTo(order);
                });
        });

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(deliveryLog.forTopic(KafkaTopologyConfig.PRIORITY_TOPIC))
                .anySatisfy(entry -> assertThat(entry.note()).contains("simulateError"));
        });

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(deliveryLog.forTopic(KafkaTopologyConfig.AUDIT_TOPIC))
                .anySatisfy(entry -> assertThat(entry.payload()).isEqualTo(order));
        });
    }

    @Test
    void broadcastIsDeliveredToAllSubscribers() {
        BroadcastMessage announcement = messagingService.broadcast(
            new AnnouncementRequest("System maintenance tonight")
        );

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            assertThat(deliveryLog.forTopic(KafkaTopologyConfig.BROADCAST_TOPIC))
                .hasSizeGreaterThanOrEqualTo(2)
                .anySatisfy(entry -> {
                    assertThat(entry.note()).contains("email subscriber");
                    assertThat(entry.payload()).isEqualTo(announcement);
                })
                .anySatisfy(entry -> {
                    assertThat(entry.note()).contains("sms subscriber");
                    assertThat(entry.payload()).isEqualTo(announcement);
                });
        });
    }
}
