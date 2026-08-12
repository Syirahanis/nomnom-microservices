package com.nomnom.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Consumed from the "order.delivered" topic. Published by the Delivery
 * Service once a rider completes a delivery. Customer Service already
 * consumes this same topic with an identical field layout
 * (orderId, customerCode, status) - keep both copies of this class in sync
 * if the schema ever changes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeliveredEvent {

    private String orderId;
    private String customerCode;
    private String status;
}
