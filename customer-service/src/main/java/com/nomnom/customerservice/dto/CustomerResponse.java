package com.nomnom.customerservice.dto;

import com.nomnom.customerservice.entity.AccountStatus;
import com.nomnom.customerservice.entity.Address;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerResponse {

    private String customerCode;

    private String fullName;

    private String email;

    private String phoneNumber;

    private int loyaltyPoints;

    private AccountStatus accountStatus;

    private LocalDateTime dateRegistered;

    private Address address;

}