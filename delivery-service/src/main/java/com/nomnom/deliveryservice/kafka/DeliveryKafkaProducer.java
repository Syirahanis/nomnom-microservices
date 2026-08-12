package com.nomnom.deliveryservice.kafka;

import com.nomnom.deliveryservice.event.DeliveryAssignedEvent;
import com.nomnom.deliveryservice.event.OrderDeliveredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_DELIVERY_ASSIGNED = "delivery.assigned";
    private static final String TOPIC_ORDER_DELIVERED = "order.delivered";

    // Published after a rider is assigned to a confirmed order
    public void publishDeliveryAssigned(DeliveryAssignedEvent event) {
        kafkaTemplate.send(
                TOPIC_DELIVERY_ASSIGNED,
                event.getOrderId(),
                event
        );
    }

    // Published after a delivery is completed
    public void publishOrderDelivered(OrderDeliveredEvent event) {
        kafkaTemplate.send(
                TOPIC_ORDER_DELIVERED,
                event.getOrderId(),
                event
        );
    }
}
