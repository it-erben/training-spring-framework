package tech.erben.springboot.amqpdemo.messaging;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tech.erben.springboot.amqpdemo.config.RabbitTopologyConfig;
import tech.erben.springboot.amqpdemo.model.BroadcastMessage;
import tech.erben.springboot.amqpdemo.model.OrderMessage;
import tech.erben.springboot.amqpdemo.service.InMemoryDeliveryLog;

import java.io.IOException;

@Component
public class OrderListeners {

    private final InMemoryDeliveryLog deliveryLog;

    public OrderListeners(InMemoryDeliveryLog deliveryLog) {
        this.deliveryLog = deliveryLog;
    }

    @RabbitListener(queues = RabbitTopologyConfig.STANDARD_QUEUE)
    public void handleStandard(OrderMessage message) {
        deliveryLog.record(
            RabbitTopologyConfig.STANDARD_QUEUE,
            "standard order (auto-ack)",
            message
        );
    }

    @RabbitListener(
        queues = RabbitTopologyConfig.PRIORITY_QUEUE,
        containerFactory = "manualAckContainerFactory"
    )
    public void handlePriority(
        OrderMessage message,
        Channel channel,
        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        deliveryLog.record(
            RabbitTopologyConfig.PRIORITY_QUEUE,
            "priority order (manual-ack)",
            message
        );
        if (message.simulateError()) {
            deliveryLog.record(
                RabbitTopologyConfig.PRIORITY_QUEUE,
                "simulateError=true -> basicNack to DLQ",
                message
            );
            channel.basicNack(deliveryTag, false, false);
            return;
        }
        channel.basicAck(deliveryTag, false);
    }

    @RabbitListener(queues = RabbitTopologyConfig.AUDIT_QUEUE)
    public void handleAudit(OrderMessage message) {
        deliveryLog.record(
            RabbitTopologyConfig.AUDIT_QUEUE,
            "audit tap (order.#)",
            message
        );
    }

    @RabbitListener(queues = RabbitTopologyConfig.DEAD_LETTER_QUEUE)
    public void handleDeadLetter(OrderMessage message) {
        deliveryLog.record(
            RabbitTopologyConfig.DEAD_LETTER_QUEUE,
            "dead-letter received",
            message
        );
    }

    @RabbitListener(queues = RabbitTopologyConfig.EMAIL_QUEUE)
    public void handleEmailNotification(BroadcastMessage message) {
        deliveryLog.record(
            RabbitTopologyConfig.EMAIL_QUEUE,
            "fanout -> email subscriber",
            message
        );
    }

    @RabbitListener(queues = RabbitTopologyConfig.SMS_QUEUE)
    public void handleSmsNotification(BroadcastMessage message) {
        deliveryLog.record(
            RabbitTopologyConfig.SMS_QUEUE,
            "fanout -> sms subscriber",
            message
        );
    }
}
