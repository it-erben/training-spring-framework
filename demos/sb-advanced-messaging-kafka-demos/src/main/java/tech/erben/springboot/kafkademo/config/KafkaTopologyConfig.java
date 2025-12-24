package tech.erben.springboot.kafkademo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.kafka.autoconfigure.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaTopologyConfig {

    public static final String STANDARD_TOPIC = "orders.standard";
    public static final String PRIORITY_TOPIC = "orders.priority";
    public static final String AUDIT_TOPIC = "orders.audit";
    public static final String DEAD_LETTER_TOPIC = "orders.dlt";
    public static final String BROADCAST_TOPIC = "notifications.broadcast";

    public static final String STANDARD_GROUP = "orders-standard";
    public static final String PRIORITY_GROUP = "orders-priority";
    public static final String AUDIT_GROUP = "orders-audit";
    public static final String DLT_GROUP = "orders-dlt";
    public static final String EMAIL_GROUP = "notifications-email";
    public static final String SMS_GROUP = "notifications-sms";

    @Bean
    public NewTopic standardOrdersTopic() {
        return TopicBuilder.name(STANDARD_TOPIC).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic priorityOrdersTopic() {
        return TopicBuilder.name(PRIORITY_TOPIC).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auditTopic() {
        return TopicBuilder.name(AUDIT_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name(DEAD_LETTER_TOPIC).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic broadcastTopic() {
        return TopicBuilder.name(BROADCAST_TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
        KafkaTemplate<Object, Object> kafkaTemplate
    ) {
        return new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, ex) -> new TopicPartition(DEAD_LETTER_TOPIC, record.partition())
        );
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
        DeadLetterPublishingRecoverer deadLetterPublishingRecoverer
    ) {
        return new DefaultErrorHandler(
            deadLetterPublishingRecoverer,
            new FixedBackOff(500L, 1)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
        ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
        ConsumerFactory<Object, Object> consumerFactory,
        DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);
        factory.setConcurrency(2);
        return factory;
    }

    @Bean(name = "manualAckKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<Object, Object> manualAckKafkaListenerContainerFactory(
        ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
        ConsumerFactory<Object, Object> consumerFactory,
        DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setConcurrency(1);
        return factory;
    }
}
