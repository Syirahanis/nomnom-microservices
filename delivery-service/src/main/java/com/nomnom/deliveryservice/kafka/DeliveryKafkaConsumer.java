package com.nomnom.deliveryservice.kafka;

import com.nomnom.deliveryservice.event.OrderConfirmedEvent;
import com.nomnom.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes the "order.confirmed" topic published by the Restaurant service
 */
@Component
@RequiredArgsConstructor
public class DeliveryKafkaConsumer {

    private final DeliveryService deliveryService;

    @KafkaListener(
            topics = "order.confirmed",
            groupId = "delivery-service",
            containerFactory = "orderConfirmedKafkaListenerContainerFactory"
    )
    public void consumeOrderConfirmed(OrderConfirmedEvent event) {

        System.out.println("Received OrderConfirmed event: " + event);

        deliveryService.handleOrderConfirmed(event);
    }
}
