package com.nomnom.customerservice.kafka;

import com.nomnom.customerservice.event.OrderDeliveredEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CustomerKafkaConsumer {

    @KafkaListener(
            topics = "order.delivered",
            groupId = "customer-service"
    )
    public void consumeOrderDelivered(OrderDeliveredEvent event) {

        System.out.println(
                "Received OrderDelivered event: " + event
        );
    }
}