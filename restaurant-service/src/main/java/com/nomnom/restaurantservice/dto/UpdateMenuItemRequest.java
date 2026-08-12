package com.nomnom.restaurantservice.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateMenuItemRequest {

    private String name;
    private String description;

    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    private String category;

    // null means "don't change", true/false means update availability
    private Boolean available;
}
