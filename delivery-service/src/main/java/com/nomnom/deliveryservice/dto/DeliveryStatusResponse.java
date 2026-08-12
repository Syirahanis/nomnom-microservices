package com.nomnom.deliveryservice.dto;

import com.nomnom.deliveryservice.entity.DeliveryStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * use in /api/deliveries/{id}/status
 */
@Data
public class DeliveryStatusResponse {

    private String deliveryCode;

    private String orderId;

    private String riderId;

    private DeliveryStatus status;

    private LocalDateTime assignedAt;

    private LocalDateTime deliveredAt;
}
