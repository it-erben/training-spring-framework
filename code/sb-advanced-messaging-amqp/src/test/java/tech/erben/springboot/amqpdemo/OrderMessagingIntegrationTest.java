package tech.erben.springboot.amqpdemo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import tech.erben.springboot.amqpdemo.config.RabbitTopologyConfig;
import tech.erben.springboot.amqpdemo.model.BroadcastMessage;
import tech.erben.springboot.amqpdemo.model.OrderMessage;
import tech.erben.springboot.amqpdemo.service.InMemoryDeliveryLog;
import tech.erben.springboot.amqpdemo.service.OrderMessagingService;
import tech.erben.springboot.amqpdemo.web.AnnouncementRequest;
import tech.erben.springboot.amqpdemo.web.OrderRequest;

@Testcontainers
@SpringBootTest
class OrderMessagingIntegrationTest {

    @Container
    static final RabbitMQContainer rabbit = new RabbitMQContainer(
        DockerImageName.parse("rabbitmq:3.13-management")
    )
        .withAdminUser("test")
        .withAdminPassword("test");

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
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

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(deliveryLog.forQueue(RabbitTopologyConfig.STANDARD_QUEUE))
                .anySatisfy(entry -> {
                    assertThat(entry.note()).contains("standard order");
                    assertThat(entry.payload()).isEqualTo(order);
                });
        });

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(deliveryLog.forQueue(RabbitTopologyConfig.AUDIT_QUEUE))
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

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(deliveryLog.forQueue(RabbitTopologyConfig.PRIORITY_QUEUE))
                .anySatisfy(entry -> {
                    assertThat(entry.note()).contains("priority order");
                    assertThat(entry.payload()).isEqualTo(order);
                });
        });

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(deliveryLog.forQueue(RabbitTopologyConfig.AUDIT_QUEUE))
                .anySatisfy(entry -> {
                    assertThat(entry.note()).contains("audit tap");
                    assertThat(entry.payload()).isEqualTo(order);
                });
        });

        await()
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() ->
                assertThat(deliveryLog.forQueue(RabbitTopologyConfig.DEAD_LETTER_QUEUE))
                    .isEmpty()
            );
    }

    @Test
    void failingPriorityOrderIsDeadLetteredAndAudited() {
        OrderMessage order = messagingService.sendOrder(
            new OrderRequest("unlucky-user", "fragile", 1, true, true)
        );

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(deliveryLog.forQueue(RabbitTopologyConfig.PRIORITY_QUEUE))
                .anySatisfy(entry -> assertThat(entry.note()).contains("simulateError"));
        });

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(deliveryLog.forQueue(RabbitTopologyConfig.DEAD_LETTER_QUEUE))
                .anySatisfy(entry -> {
                    assertThat(entry.note()).contains("dead-letter");
                    assertThat(entry.payload()).isEqualTo(order);
                });
        });

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(deliveryLog.forQueue(RabbitTopologyConfig.AUDIT_QUEUE))
                .anySatisfy(entry -> assertThat(entry.payload()).isEqualTo(order));
        });
    }

    @Test
    void broadcastIsDeliveredToAllSubscribers() {
        BroadcastMessage announcement = messagingService.broadcast(
            new AnnouncementRequest("System maintenance tonight")
        );

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(deliveryLog.forQueue(RabbitTopologyConfig.EMAIL_QUEUE))
                .anySatisfy(entry -> {
                    assertThat(entry.note()).contains("email subscriber");
                    assertThat(entry.payload()).isEqualTo(announcement);
                });
        });

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            assertThat(deliveryLog.forQueue(RabbitTopologyConfig.SMS_QUEUE))
                .anySatisfy(entry -> {
                    assertThat(entry.note()).contains("sms subscriber");
                    assertThat(entry.payload()).isEqualTo(announcement);
                });
        });
    }
}
