package com.nomnom.orderservice.dto;

import com.nomnom.orderservice.entity.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {

    private String orderCode;
    private String customerCode;
    private String restaurantCode;
    private String deliveryAddress;
    private List<OrderItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal totalAmount;
    private String currency;
    private OrderStatus status;
    private LocalDateTime placedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime deliveredAt;

}
