package com.nomnom.deliveryservice.dto;

import com.nomnom.deliveryservice.entity.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDeliveryStatusRequest {

    @NotNull(message = "status is required")
    private DeliveryStatus status;
}
