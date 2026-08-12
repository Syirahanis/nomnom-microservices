package com.nomnom.deliveryservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAssignedEvent {

    private String deliveryCode;
    private String orderCode;
    private String riderId;
    private String deliveryAddress;
    private String assignedAt;
}
