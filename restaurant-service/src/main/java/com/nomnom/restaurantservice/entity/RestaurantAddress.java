package com.nomnom.restaurantservice.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DDD Value Object: RestaurantAddress
// An address has no identity of its own — it only makes sense
// as part of a Restaurant aggregate. Two addresses with the same
// values are considered equal, which is typical for value objects.
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class RestaurantAddress {

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
