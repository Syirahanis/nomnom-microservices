package com.nomnom.restaurantservice.service;

import com.nomnom.restaurantservice.dto.*;

import java.util.List;

public interface RestaurantService {

    RestaurantResponse registerRestaurant(RegisterRestaurantRequest request);

    RestaurantResponse getRestaurantByCode(String restaurantCode);

    List<RestaurantResponse> getAllRestaurants();

    RestaurantResponse updateRestaurant(String restaurantCode, UpdateRestaurantRequest request);

    void deleteRestaurant(String restaurantCode);

    // Menu Item operations
    MenuItemResponse addMenuItem(String restaurantCode, AddMenuItemRequest request);

    MenuItemResponse updateMenuItem(String restaurantCode, String menuItemCode, UpdateMenuItemRequest request);

    void deleteMenuItem(String restaurantCode, String menuItemCode);

    List<MenuItemResponse> getMenuByRestaurantCode(String restaurantCode);
}
