package com.nomnom.orderservice.controller;

import com.nomnom.orderservice.dto.OrderResponse;
import com.nomnom.orderservice.dto.PlaceOrderRequest;
import com.nomnom.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Place a new order
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request) {

        OrderResponse response = orderService.placeOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all orders
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {

        return ResponseEntity.ok(
                orderService.getAllOrders()
        );
    }

    // Get order by order code
    @GetMapping("/{orderCode}")
    public ResponseEntity<OrderResponse> getOrderByCode(
            @PathVariable String orderCode) {

        return ResponseEntity.ok(
                orderService.getOrderByCode(orderCode)
        );
    }

    // Get all orders placed by a given customer
    @GetMapping("/customer/{customerCode}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(
            @PathVariable String customerCode) {

        return ResponseEntity.ok(
                orderService.getOrdersByCustomerCode(customerCode)
        );
    }

    // Cancel an order (only allowed while still PLACED)
    @PutMapping("/{orderCode}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable String orderCode) {

        return ResponseEntity.ok(
                orderService.cancelOrder(orderCode)
        );
    }
}
