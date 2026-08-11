package com.nomnom.customerservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeliveredEvent {

    private String orderId;
    private String customerCode;
    private String status;
}