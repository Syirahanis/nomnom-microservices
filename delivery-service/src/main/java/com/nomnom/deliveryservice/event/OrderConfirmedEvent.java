package com.nomnom.deliveryservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmedEvent {

    private String orderCode;
    private String restaurantCode;
    private String status;
    private String restaurantName;
    private String customerCode;
    private String confirmedAt;
}
