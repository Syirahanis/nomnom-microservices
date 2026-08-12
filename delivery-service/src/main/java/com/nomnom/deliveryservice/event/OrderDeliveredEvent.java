package com.nomnom.deliveryservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeliveredEvent {

    private String orderCode;
    private String customerCode;
    private String status;
}
