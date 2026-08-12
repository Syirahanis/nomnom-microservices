package com.nomnom.restaurantservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// DDD Domain Event: OrderPlacedEvent
// Published by Order Service when a customer places an order.
// Restaurant Service consumes this event to validate item availability
// and then decides whether to confirm or reject the order.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {

    private String orderCode;
    private String customerCode;
    private String restaurantCode;
    private List<OrderItemDetail> items;
    private BigDecimal totalAmount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDetail {
        private String menuItemCode;
        private String itemName;
        private int quantity;
        private BigDecimal unitPrice;
    }
}
