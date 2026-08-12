package com.nomnom.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Consumed from the "order.confirmed" topic. Published by the Restaurant &
 * Catalog Service after it validates item availability for an OrderPlaced
 * event. Field names must stay in sync with that service's own copy of this
 * event schema (agreed cross-service contract - see PID integration
 * constraint on shared event conventions).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent {

    private String orderId;
    private String restaurantCode;
    private String status;
}
