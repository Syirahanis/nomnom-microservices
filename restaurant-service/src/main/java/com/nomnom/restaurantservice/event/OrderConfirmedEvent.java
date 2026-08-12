package com.nomnom.restaurantservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DDD Domain Event: OrderConfirmedEvent
// Published to topic "order.confirmed".
// Consumed by Order Service and Delivery Service.
// Core fields match Order Service schema: orderCode, restaurantCode, status.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent {

    private String orderCode;
    private String restaurantCode;
    private String status;           // "CONFIRMED" or "REJECTED"
    private String restaurantName;
    private String customerCode;
    private String confirmedAt;
}
