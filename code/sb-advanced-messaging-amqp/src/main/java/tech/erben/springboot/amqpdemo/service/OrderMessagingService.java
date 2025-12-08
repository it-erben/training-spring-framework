package tech.erben.springboot.amqpdemo.service;

import java.time.Instant;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tech.erben.springboot.amqpdemo.config.RabbitTopologyConfig;
import tech.erben.springboot.amqpdemo.model.BroadcastMessage;
import tech.erben.springboot.amqpdemo.model.OrderMessage;
import tech.erben.springboot.amqpdemo.web.AnnouncementRequest;
import tech.erben.springboot.amqpdemo.web.OrderRequest;

@Service
public class OrderMessagingService {

    private final RabbitTemplate rabbitTemplate;

    public OrderMessagingService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public OrderMessage sendOrder(OrderRequest request) {
        Instant now = Instant.now();
        String routingKey = request.priority() ? "order.priority" : "order.standard";
        OrderMessage message = new OrderMessage(
            UUID.randomUUID().toString(),
            request.customer(),
            request.item(),
            request.quantity(),
            request.priority(),
            request.simulateError(),
            routingKey,
            now
        );
        rabbitTemplate.convertAndSend(
            RabbitTopologyConfig.ORDERS_EXCHANGE,
            routingKey,
            message
        );
        return message;
    }

    public BroadcastMessage broadcast(AnnouncementRequest request) {
        BroadcastMessage message = new BroadcastMessage(request.message(), Instant.now());
        rabbitTemplate.convertAndSend(
            RabbitTopologyConfig.BROADCAST_EXCHANGE,
            "",
            message
        );
        return message;
    }
}
