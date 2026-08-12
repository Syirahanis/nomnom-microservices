package com.nomnom.deliveryservice.dto;

import com.nomnom.deliveryservice.entity.DeliveryStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeliveryResponse {

    private Long id;

    private String deliveryCode;

    private String orderId;

    private String riderId;

    private DeliveryStatus status;

    private String deliveryAddress;

    private LocalDateTime assignedAt;

    private LocalDateTime inTransitAt;

    private LocalDateTime deliveredAt;
}
