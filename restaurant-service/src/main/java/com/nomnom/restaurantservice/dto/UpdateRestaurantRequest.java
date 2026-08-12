package com.nomnom.restaurantservice.dto;

import com.nomnom.restaurantservice.entity.RestaurantAddress;
import lombok.Data;

@Data
public class UpdateRestaurantRequest {

    private String name;
    private String description;
    private String cuisine;
    private String ownerName;
    private String contactPhone;
    private String contactEmail;
    private String openingHours;
    private RestaurantAddress address;
}
