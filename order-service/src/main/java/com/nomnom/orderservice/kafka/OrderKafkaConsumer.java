package com.nomnom.orderservice.kafka;

import com.nomnom.orderservice.event.OrderConfirmedEvent;
import com.nomnom.orderservice.event.OrderDeliveredEvent;
import com.nomnom.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderKafkaConsumer {

    private final OrderService orderService;

    @KafkaListener(
            topics = "order.confirmed",
            groupId = "order-service",
            containerFactory = "orderConfirmedKafkaListenerContainerFactory"
    )
    public void consumeOrderConfirmed(OrderConfirmedEvent event) {

        System.out.println("Received OrderConfirmed event: " + event);

        orderService.handleOrderConfirmed(event.getorderCode());
    }

    @KafkaListener(
            topics = "order.delivered",
            groupId = "order-service",
            containerFactory = "orderDeliveredKafkaListenerContainerFactory"
    )
    public void consumeOrderDelivered(OrderDeliveredEvent event) {

        System.out.println("Received OrderDelivered event: " + event);

        orderService.handleOrderDelivered(event.getorderCode());
    }
}
