package com.nomnom.orderservice.kafka;

import com.nomnom.orderservice.event.OrderConfirmedEvent;
import com.nomnom.orderservice.event.OrderDeliveredEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Order Service consumes two different event types on two different
 * topics - OrderConfirmedEvent from "order.confirmed" and
 * OrderDeliveredEvent from "order.delivered". Spring Boot's
 * autoconfigured Kafka consumer factory only supports a single default
 * payload type (that's why Customer Service, which only consumes one
 * event type, could rely on the auto-configured factory). Order Service
 * needs one ConsumerFactory / listener container factory per event type
 * instead, each with its target class set explicitly and Kafka type
 * headers ignored, so deserialization does not depend on the producer's
 * fully-qualified class name (which lives in a different service's
 * package).
 *
 * Note for the team: JacksonJsonDeserializer is used here to match the
 * producer-side JacksonJsonSerializer already configured in
 * application.properties. If this Spring Boot/Spring Kafka version
 * exposes slightly different method names on the deserializer, adjust
 * accordingly - the important part is: target type set per event class,
 * type headers ignored, trusted packages open.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    @Bean
    public ConsumerFactory<String, OrderConfirmedEvent> orderConfirmedConsumerFactory() {
        JacksonJsonDeserializer<OrderConfirmedEvent> deserializer =
                new JacksonJsonDeserializer<>(OrderConfirmedEvent.class);
        deserializer.ignoreTypeHeaders();
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                baseConsumerProps(),
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderConfirmedEvent> orderConfirmedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderConfirmedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderConfirmedConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, OrderDeliveredEvent> orderDeliveredConsumerFactory() {
        JacksonJsonDeserializer<OrderDeliveredEvent> deserializer =
                new JacksonJsonDeserializer<>(OrderDeliveredEvent.class);
        deserializer.ignoreTypeHeaders();
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                baseConsumerProps(),
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderDeliveredEvent> orderDeliveredKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderDeliveredEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderDeliveredConsumerFactory());
        return factory;
    }
}
