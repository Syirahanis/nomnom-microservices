package com.nomnom.customerservice.kafka;

import com.nomnom.customerservice.event.OrderDeliveredEvent;
import com.nomnom.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerKafkaConsumer {

    private final CustomerService customerService;

    // Points awarded per completed order. Kept as a simple constant for now;
    // move to application.properties if this ever needs to vary by
    // order value/promotions.
    private static final int POINTS_PER_DELIVERED_ORDER = 10;

    @KafkaListener(
            topics = "order.delivered",
            groupId = "customer-service"
    )
    public void consumeOrderDelivered(OrderDeliveredEvent event) {

        System.out.println(
                "Received OrderDelivered event: " + event
        );

        customerService.awardLoyaltyPoints(
                event.getCustomerCode(),
                POINTS_PER_DELIVERED_ORDER
        );
    }
}
