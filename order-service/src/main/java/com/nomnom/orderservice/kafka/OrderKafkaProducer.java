package com.nomnom.orderservice.kafka;

import com.nomnom.orderservice.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderKafkaProducer {

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    private static final String TOPIC = "order.placed";

    public void publishOrderPlaced(OrderPlacedEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event.getOrderId(),
                event
        );
    }
}
