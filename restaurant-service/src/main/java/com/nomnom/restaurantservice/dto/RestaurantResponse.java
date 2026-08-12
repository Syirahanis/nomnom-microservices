package com.nomnom.restaurantservice.dto;

import com.nomnom.restaurantservice.entity.RestaurantAddress;
import com.nomnom.restaurantservice.entity.RestaurantStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RestaurantResponse {

    private String restaurantCode;
    private String name;
    private String description;
    private String cuisine;
    private String ownerName;
    private String contactPhone;
    private String contactEmail;
    private String openingHours;
    private RestaurantStatus status;
    private LocalDateTime dateRegistered;
    private RestaurantAddress address;
    private List<MenuItemResponse> menuItems;
}
