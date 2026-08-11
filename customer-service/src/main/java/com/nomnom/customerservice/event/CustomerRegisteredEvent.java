package com.nomnom.customerservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRegisteredEvent {

    private String customerCode;
    private String fullName;
    private String email;
    private String phoneNumber;
}