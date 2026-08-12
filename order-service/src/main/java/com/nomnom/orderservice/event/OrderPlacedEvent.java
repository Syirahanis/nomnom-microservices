package com.nomnom.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Published to the "order.placed" topic once an order has been created.
 * The Restaurant & Catalog Service is expected to consume this to validate
 * item availability and publish OrderConfirmed back.
 */
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
