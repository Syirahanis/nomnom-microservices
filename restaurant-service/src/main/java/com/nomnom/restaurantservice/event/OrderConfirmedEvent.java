package com.nomnom.restaurantservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DDD Domain Event: OrderConfirmedEvent
// Published by Restaurant Service after it validates item availability
// and confirms the order. Consumed by Order Service (to update order status)
// and Delivery Service (to assign a rider).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent {

    private String orderCode;
    private String restaurantCode;
    private String restaurantName;
    private String customerCode;
    private String confirmedAt;
}
