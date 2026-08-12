package com.nomnom.restaurantservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// DDD Domain Event: OrderPlacedEvent
// Published by Order Service to topic "order.placed".
// Field names must match Order Service's OrderPlacedEvent exactly —
// orderId and OrderedLineItem are the names Order Service uses.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {

    private String orderId;            // Order Service uses orderId, not orderCode
    private String customerCode;
    private String restaurantCode;
    private List<OrderedLineItem> items;
    private BigDecimal totalAmount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderedLineItem {   // Order Service uses OrderedLineItem, not OrderItemDetail
        private String menuItemCode;
        private String itemName;
        private int quantity;
        private BigDecimal unitPrice;
    }
}
