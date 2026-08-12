package com.nomnom.restaurantservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DDD Domain Event: RestaurantRegisteredEvent
// Published when a new restaurant is registered in the system.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRegisteredEvent {

    private String restaurantCode;
    private String name;
    private String cuisine;
    private String contactPhone;
}
