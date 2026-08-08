package com.nomnom.customerservice.dto;

import com.nomnom.customerservice.entity.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterCustomerRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;

    private LocalDate dateOfBirth;

    @Valid
    private Address address;

}