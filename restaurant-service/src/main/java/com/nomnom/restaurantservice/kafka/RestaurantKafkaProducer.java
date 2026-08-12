package com.nomnom.restaurantservice.kafka;

import com.nomnom.restaurantservice.event.OrderConfirmedEvent;
import com.nomnom.restaurantservice.event.RestaurantRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_RESTAURANT_REGISTERED = "restaurant.registered";
    private static final String TOPIC_ORDER_CONFIRMED = "order.confirmed";

    // Published when a new restaurant signs up
    public void publishRestaurantRegistered(RestaurantRegisteredEvent event) {
        kafkaTemplate.send(
                TOPIC_RESTAURANT_REGISTERED,
                event.getRestaurantCode(),
                event
        );
    }

    // Published after Restaurant Service validates the order and confirms it
    public void publishOrderConfirmed(OrderConfirmedEvent event) {
        kafkaTemplate.send(
                TOPIC_ORDER_CONFIRMED,
                event.getOrderId(),
                event
        );
    }
}
