package com.nomnom.customerservice.kafka;

import com.nomnom.customerservice.event.CustomerRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerKafkaProducer {

    private final KafkaTemplate<String, CustomerRegisteredEvent> kafkaTemplate;

    private static final String TOPIC = "customer.registered";

    public void publishCustomerRegistered(CustomerRegisteredEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event.getCustomerCode(),
                event
        );
    }
}