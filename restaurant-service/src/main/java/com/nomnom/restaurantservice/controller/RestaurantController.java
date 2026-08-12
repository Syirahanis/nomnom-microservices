package com.nomnom.restaurantservice.controller;

import com.nomnom.restaurantservice.dto.*;
import com.nomnom.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // ============================================================
    // RESTAURANT ENDPOINTS
    // ============================================================

    // Register a new restaurant
    @PostMapping
    public ResponseEntity<RestaurantResponse> registerRestaurant(
            @Valid @RequestBody RegisterRestaurantRequest request) {

        RestaurantResponse response = restaurantService.registerRestaurant(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all restaurants
    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    // Get restaurant by restaurant code
    @GetMapping("/{restaurantCode}")
    public ResponseEntity<RestaurantResponse> getRestaurantByCode(
            @PathVariable String restaurantCode) {

        return ResponseEntity.ok(
                restaurantService.getRestaurantByCode(restaurantCode));
    }

    // Update restaurant details
    @PutMapping("/{restaurantCode}")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable String restaurantCode,
            @Valid @RequestBody UpdateRestaurantRequest request) {

        return ResponseEntity.ok(
                restaurantService.updateRestaurant(restaurantCode, request));
    }

    // Delete (suspend) a restaurant
    @DeleteMapping("/{restaurantCode}")
    public ResponseEntity<Void> deleteRestaurant(
            @PathVariable String restaurantCode) {

        restaurantService.deleteRestaurant(restaurantCode);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // MENU ITEM ENDPOINTS
    // ============================================================

    // Get all menu items for a restaurant
    @GetMapping("/{restaurantCode}/menu")
    public ResponseEntity<List<MenuItemResponse>> getMenu(
            @PathVariable String restaurantCode) {

        return ResponseEntity.ok(
                restaurantService.getMenuByRestaurantCode(restaurantCode));
    }

    // Add a menu item to a restaurant
    @PostMapping("/{restaurantCode}/menu")
    public ResponseEntity<MenuItemResponse> addMenuItem(
            @PathVariable String restaurantCode,
            @Valid @RequestBody AddMenuItemRequest request) {

        MenuItemResponse response = restaurantService.addMenuItem(restaurantCode, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Update a menu item
    @PutMapping("/{restaurantCode}/menu/{menuItemCode}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable String restaurantCode,
            @PathVariable String menuItemCode,
            @Valid @RequestBody UpdateMenuItemRequest request) {

        return ResponseEntity.ok(
                restaurantService.updateMenuItem(restaurantCode, menuItemCode, request));
    }

    // Delete a menu item
    @DeleteMapping("/{restaurantCode}/menu/{menuItemCode}")
    public ResponseEntity<Void> deleteMenuItem(
            @PathVariable String restaurantCode,
            @PathVariable String menuItemCode) {

        restaurantService.deleteMenuItem(restaurantCode, menuItemCode);
        return ResponseEntity.noContent().build();
    }
}
