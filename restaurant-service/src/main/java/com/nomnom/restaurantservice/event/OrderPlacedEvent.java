package com.nomnom.restaurantservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// DDD Domain Event: OrderPlacedEvent
// Published by Order Service to topic "order.placed".
// Field names must match Order Service's OrderPlacedEvent exactly:
//   - field is orderCode (not orderCode)
//   - inner class is OrderedLineItem with menuItemCode + quantity only
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {

    private String orderCode;
    private String customerCode;
    private String restaurantCode;
    private List<OrderedLineItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderedLineItem {
        private String menuItemCode;
        private int quantity;
    }
}
