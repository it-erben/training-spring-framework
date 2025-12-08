package tech.erben.springboot.amqpdemo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopologyConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitTopologyConfig.class);

    public static final String ORDERS_EXCHANGE = "orders.topic";
    public static final String BROADCAST_EXCHANGE = "notifications.fanout";
    public static final String DEAD_LETTER_EXCHANGE = "orders.dlx";

    public static final String STANDARD_QUEUE = "orders.standard";
    public static final String PRIORITY_QUEUE = "orders.priority";
    public static final String AUDIT_QUEUE = "orders.audit";
    public static final String DEAD_LETTER_QUEUE = "orders.dlq";
    public static final String EMAIL_QUEUE = "notifications.email";
    public static final String SMS_QUEUE = "notifications.sms";

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(ORDERS_EXCHANGE);
    }

    @Bean
    public FanoutExchange notificationsExchange() {
        return new FanoutExchange(BROADCAST_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue standardQueue() {
        return QueueBuilder
            .durable(STANDARD_QUEUE)
            .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", DEAD_LETTER_QUEUE)
            .build();
    }

    @Bean
    public Queue priorityQueue() {
        return QueueBuilder
            .durable(PRIORITY_QUEUE)
            .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", DEAD_LETTER_QUEUE)
            .build();
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE).build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE).build();
    }

    @Bean
    public Queue smsQueue() {
        return QueueBuilder.durable(SMS_QUEUE).build();
    }

    @Bean
    public Binding standardBinding(Queue standardQueue, TopicExchange ordersExchange) {
        return BindingBuilder
            .bind(standardQueue)
            .to(ordersExchange)
            .with("order.standard.#");
    }

    @Bean
    public Binding priorityBinding(Queue priorityQueue, TopicExchange ordersExchange) {
        return BindingBuilder
            .bind(priorityQueue)
            .to(ordersExchange)
            .with("order.priority.#");
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(auditQueue).to(ordersExchange).with("order.#");
    }

    @Bean
    public Binding deadLetterBinding(
        Queue deadLetterQueue,
        DirectExchange deadLetterExchange
    ) {
        return BindingBuilder
            .bind(deadLetterQueue)
            .to(deadLetterExchange)
            .with(DEAD_LETTER_QUEUE);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, FanoutExchange notificationsExchange) {
        return BindingBuilder.bind(emailQueue).to(notificationsExchange);
    }

    @Bean
    public Binding smsBinding(Queue smsQueue, FanoutExchange notificationsExchange) {
        return BindingBuilder.bind(smsQueue).to(notificationsExchange);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
        ConnectionFactory connectionFactory,
        MessageConverter messageConverter
    ) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.warn("Publisher NACK for {} cause={}", correlationData, cause);
            }
        });
        rabbitTemplate.setReturnsCallback(returned ->
            log.warn(
                "Returned message exchange={} routingKey={} replyCode={} replyText={}",
                returned.getExchange(),
                returned.getRoutingKey(),
                returned.getReplyCode(),
                returned.getReplyText()
            )
        );
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory manualAckContainerFactory(
        SimpleRabbitListenerContainerFactoryConfigurer configurer,
        ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setPrefetchCount(5);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
