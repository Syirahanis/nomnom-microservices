package com.nomnom.customerservice.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Address {

    @NotBlank(message = "Unit/House Number is required")
    private String unitOrHouseNumber;

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "Postcode is required")
    private String postcode;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Country is required")
    private String country;

}