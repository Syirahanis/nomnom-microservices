package com.nomnom.restaurantservice.dto;

import com.nomnom.restaurantservice.entity.RestaurantAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRestaurantRequest {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    private String description;

    private String cuisine;

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @NotBlank(message = "Contact phone is required")
    private String contactPhone;

    private String contactEmail;

    private String openingHours;

    @NotNull(message = "Address is required")
    @Valid
    private RestaurantAddress address;
}
