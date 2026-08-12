package com.nomnom.orderservice.service;

import com.nomnom.orderservice.dto.OrderResponse;
import com.nomnom.orderservice.dto.PlaceOrderRequest;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(PlaceOrderRequest request);

    OrderResponse getOrderByCode(String orderCode);

    List<OrderResponse> getAllOrders();

    List<OrderResponse> getOrdersByCustomerCode(String customerCode);

    OrderResponse cancelOrder(String orderCode);

    /** Invoked by OrderKafkaConsumer when an order.confirmed event arrives. */
    void handleOrderConfirmed(String orderCode);

    /** Invoked by OrderKafkaConsumer when an order.delivered event arrives. */
    void handleOrderDelivered(String orderCode);

}
