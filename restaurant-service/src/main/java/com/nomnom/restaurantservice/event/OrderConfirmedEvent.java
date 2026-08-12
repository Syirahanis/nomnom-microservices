package com.nomnom.restaurantservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DDD Domain Event: OrderConfirmedEvent
// Published to topic "order.confirmed". Consumed by Order Service and Delivery Service.
// Field names must match Order Service's OrderConfirmedEvent exactly:
//   orderId, restaurantCode, status
// Order Service reads the "status" field to decide CONFIRMED or REJECTED.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent {

    private String orderId;         // Order Service uses orderId, not orderCode
    private String restaurantCode;
    private String status;          // "CONFIRMED" or "REJECTED" — required by Order Service
    private String restaurantName;
    private String customerCode;
    private String confirmedAt;
}
