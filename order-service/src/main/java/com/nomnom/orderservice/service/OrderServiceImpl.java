package com.nomnom.orderservice.service;

import com.nomnom.orderservice.client.CustomerServiceClient;
import com.nomnom.orderservice.dto.OrderItemRequest;
import com.nomnom.orderservice.dto.OrderItemResponse;
import com.nomnom.orderservice.dto.OrderResponse;
import com.nomnom.orderservice.dto.PlaceOrderRequest;
import com.nomnom.orderservice.entity.Money;
import com.nomnom.orderservice.entity.Order;
import com.nomnom.orderservice.entity.OrderItem;
import com.nomnom.orderservice.entity.OrderStatus;
import com.nomnom.orderservice.event.OrderPlacedEvent;
import com.nomnom.orderservice.exception.InvalidOrderStateException;
import com.nomnom.orderservice.exception.ResourceNotFoundException;
import com.nomnom.orderservice.kafka.OrderKafkaProducer;
import com.nomnom.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerServiceClient customerServiceClient;
    private final PricingService pricingService;
    private final OrderKafkaProducer orderKafkaProducer;

    @Override
    public OrderResponse placeOrder(PlaceOrderRequest request) {

        // Synchronous REST call - Order Service will not create an order
        // for a customer that Customer Service does not recognise.
        customerServiceClient.verifyCustomerExists(request.getCustomerCode());

        Order order = Order.place(
                request.getCustomerCode(),
                request.getRestaurantCode(),
                request.getDeliveryAddress(),
                generateOrderCode());

        for (OrderItemRequest itemRequest : request.getItems()) {
            order.addItem(
                    itemRequest.getMenuItemCode(),
                    itemRequest.getItemName(),
                    itemRequest.getQuantity(),
                    Money.of(itemRequest.getUnitPrice()));
        }

        // Domain service decides the delivery fee policy; the aggregate
        // just applies whatever it is told.
        Money deliveryFee = pricingService.calculateDeliveryFee(order.getSubtotal());
        order.applyDeliveryFee(deliveryFee);

        Order savedOrder = orderRepository.save(order);

        publishOrderPlaced(savedOrder);

        return toResponse(savedOrder);
    }

    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis();
    }

    private void publishOrderPlaced(Order order) {

        List<OrderPlacedEvent.OrderedLineItem> lineItems = order.getItems().stream()
                .map(item -> new OrderPlacedEvent.OrderedLineItem(
                        item.getMenuItemCode(),
                        item.getQuantity()))
                .collect(Collectors.toList());

        OrderPlacedEvent event = new OrderPlacedEvent(
                order.getOrderCode(),
                order.getCustomerCode(),
                order.getRestaurantCode(),
                lineItems);

        orderKafkaProducer.publishOrderPlaced(event);
    }

    @Override
    public OrderResponse getOrderByCode(String orderCode) {
        return toResponse(findOrderOrThrow(orderCode));
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByCustomerCode(String customerCode) {
        return orderRepository.findByCustomerCode(customerCode).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse cancelOrder(String orderCode) {

        Order order = findOrderOrThrow(orderCode);

        try {
            order.cancel();
        } catch (IllegalStateException ex) {
            throw new InvalidOrderStateException(ex.getMessage());
        }

        return toResponse(orderRepository.save(order));
    }

    @Override
    public void handleOrderConfirmed(String orderCode) {

        orderRepository.findByOrderCode(orderCode).ifPresentOrElse(
                order -> {
                    if (order.getStatus() == OrderStatus.PLACED) {
                        order.confirm();
                        orderRepository.save(order);
                    } else {
                        System.out.println(
                                "Ignoring OrderConfirmed for " + orderCode
                                        + " - order is already " + order.getStatus());
                    }
                },
                () -> System.out.println(
                        "Ignoring OrderConfirmed for unknown order: " + orderCode)
        );
    }

    @Override
    public void handleOrderDelivered(String orderCode) {

        orderRepository.findByOrderCode(orderCode).ifPresentOrElse(
                order -> {
                    if (order.getStatus() == OrderStatus.CONFIRMED) {
                        order.markDelivered();
                        orderRepository.save(order);
                    } else {
                        System.out.println(
                                "Ignoring OrderDelivered for " + orderCode
                                        + " - order is currently " + order.getStatus());
                    }
                },
                () -> System.out.println(
                        "Ignoring OrderDelivered for unknown order: " + orderCode)
        );
    }

    private Order findOrderOrThrow(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with code: " + orderCode));
    }

    private OrderResponse toResponse(Order order) {

        OrderResponse response = new OrderResponse();

        response.setOrderCode(order.getOrderCode());
        response.setCustomerCode(order.getCustomerCode());
        response.setRestaurantCode(order.getRestaurantCode());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setItems(order.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList()));
        response.setSubtotal(order.getSubtotal().getAmount());
        response.setDeliveryFee(order.getDeliveryFee().getAmount());
        response.setTotalAmount(order.getTotalAmount().getAmount());
        response.setCurrency(order.getTotalAmount().getCurrency());
        response.setStatus(order.getStatus());
        response.setPlacedAt(order.getPlacedAt());
        response.setConfirmedAt(order.getConfirmedAt());
        response.setDeliveredAt(order.getDeliveredAt());

        return response;
    }

    private OrderItemResponse toItemResponse(OrderItem item) {

        OrderItemResponse response = new OrderItemResponse();

        response.setMenuItemCode(item.getMenuItemCode());
        response.setItemName(item.getItemName());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice().getAmount());
        response.setLineTotal(item.getLineTotal().getAmount());

        return response;
    }
}
