package com.nomnom.customerservice.dto;

import com.nomnom.customerservice.entity.Address;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateCustomerRequest {

    private String fullName;

    private String email;

    private String phoneNumber;

    private LocalDate dateOfBirth;

    private Address address;

}