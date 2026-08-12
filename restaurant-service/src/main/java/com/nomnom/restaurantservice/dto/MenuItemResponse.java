package com.nomnom.restaurantservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuItemResponse {

    private String menuItemCode;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private boolean available;
    private String restaurantCode;
}
