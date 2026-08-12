package com.nomnom.restaurantservice.service;

import com.nomnom.restaurantservice.dto.*;
import com.nomnom.restaurantservice.entity.MenuItem;
import com.nomnom.restaurantservice.entity.Restaurant;
import com.nomnom.restaurantservice.entity.RestaurantStatus;
import com.nomnom.restaurantservice.event.RestaurantRegisteredEvent;
import com.nomnom.restaurantservice.exception.DuplicateResourceException;
import com.nomnom.restaurantservice.exception.ResourceNotFoundException;
import com.nomnom.restaurantservice.kafka.RestaurantKafkaProducer;
import com.nomnom.restaurantservice.repository.MenuItemRepository;
import com.nomnom.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantKafkaProducer restaurantKafkaProducer;

    // ============================================================
    // RESTAURANT CRUD
    // ============================================================

    @Override
    public RestaurantResponse registerRestaurant(RegisterRestaurantRequest request) {

        // Business Rule: Contact phone must be unique
        if (restaurantRepository.existsByContactPhone(request.getContactPhone())) {
            throw new DuplicateResourceException("A restaurant with this phone number already exists.");
        }

        // Business Rule: Contact email must be unique (if provided)
        if (request.getContactEmail() != null && !request.getContactEmail().isBlank()) {
            if (restaurantRepository.existsByContactEmail(request.getContactEmail())) {
                throw new DuplicateResourceException("A restaurant with this email already exists.");
            }
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setRestaurantCode(generateRestaurantCode());
        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setCuisine(request.getCuisine());
        restaurant.setOwnerName(request.getOwnerName());
        restaurant.setContactPhone(request.getContactPhone());
        restaurant.setContactEmail(request.getContactEmail());
        restaurant.setOpeningHours(request.getOpeningHours());
        restaurant.setStatus(RestaurantStatus.OPEN);
        restaurant.setDateRegistered(LocalDateTime.now());
        restaurant.setAddress(request.getAddress());

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        // Publish domain event
        RestaurantRegisteredEvent event = new RestaurantRegisteredEvent(
                savedRestaurant.getRestaurantCode(),
                savedRestaurant.getName(),
                savedRestaurant.getCuisine(),
                savedRestaurant.getContactPhone()
        );
        restaurantKafkaProducer.publishRestaurantRegistered(event);

        return toRestaurantResponse(savedRestaurant);
    }

    @Override
    public RestaurantResponse getRestaurantByCode(String restaurantCode) {

        Restaurant restaurant = findRestaurantOrThrow(restaurantCode);
        return toRestaurantResponse(restaurant);
    }

    @Override
    public List<RestaurantResponse> getAllRestaurants() {

        return restaurantRepository.findAll()
                .stream()
                .map(this::toRestaurantResponse)
                .toList();
    }

    @Override
    public RestaurantResponse updateRestaurant(String restaurantCode, UpdateRestaurantRequest request) {

        Restaurant restaurant = findRestaurantOrThrow(restaurantCode);

        if (request.getName() != null && !request.getName().isBlank()) {
            restaurant.setName(request.getName());
        }

        if (request.getDescription() != null) {
            restaurant.setDescription(request.getDescription());
        }

        if (request.getCuisine() != null) {
            restaurant.setCuisine(request.getCuisine());
        }

        if (request.getOwnerName() != null && !request.getOwnerName().isBlank()) {
            restaurant.setOwnerName(request.getOwnerName());
        }

        if (request.getContactPhone() != null && !request.getContactPhone().isBlank()) {

            // Only check uniqueness if the phone number is actually changing
            if (!request.getContactPhone().equals(restaurant.getContactPhone())) {
                if (restaurantRepository.existsByContactPhone(request.getContactPhone())) {
                    throw new DuplicateResourceException("A restaurant with this phone number already exists.");
                }
            }
            restaurant.setContactPhone(request.getContactPhone());
        }

        if (request.getContactEmail() != null && !request.getContactEmail().isBlank()) {

            if (!request.getContactEmail().equals(restaurant.getContactEmail())) {
                if (restaurantRepository.existsByContactEmail(request.getContactEmail())) {
                    throw new DuplicateResourceException("A restaurant with this email already exists.");
                }
            }
            restaurant.setContactEmail(request.getContactEmail());
        }

        if (request.getOpeningHours() != null) {
            restaurant.setOpeningHours(request.getOpeningHours());
        }

        if (request.getAddress() != null) {
            restaurant.setAddress(request.getAddress());
        }

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return toRestaurantResponse(updatedRestaurant);
    }

    @Override
    public void deleteRestaurant(String restaurantCode) {

        Restaurant restaurant = findRestaurantOrThrow(restaurantCode);

        // Soft delete: mark as SUSPENDED instead of removing from DB
        restaurant.setStatus(RestaurantStatus.SUSPENDED);
        restaurantRepository.save(restaurant);
    }

    // ============================================================
    // MENU ITEM CRUD
    // ============================================================

    @Override
    public MenuItemResponse addMenuItem(String restaurantCode, AddMenuItemRequest request) {

        Restaurant restaurant = findRestaurantOrThrow(restaurantCode);

        MenuItem menuItem = new MenuItem();
        menuItem.setMenuItemCode(generateMenuItemCode());
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(request.getCategory());
        menuItem.setAvailable(true);  // New items are available by default
        menuItem.setRestaurant(restaurant);

        MenuItem savedItem = menuItemRepository.save(menuItem);
        return toMenuItemResponse(savedItem);
    }

    @Override
    public MenuItemResponse updateMenuItem(String restaurantCode,
                                           String menuItemCode,
                                           UpdateMenuItemRequest request) {

        // Make sure the restaurant exists first
        findRestaurantOrThrow(restaurantCode);

        MenuItem menuItem = menuItemRepository.findByMenuItemCode(menuItemCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu item not found: " + menuItemCode));

        // Make sure this item belongs to this restaurant
        if (!menuItem.getRestaurant().getRestaurantCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Menu item does not belong to this restaurant.");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            menuItem.setName(request.getName());
        }

        if (request.getDescription() != null) {
            menuItem.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            menuItem.setPrice(request.getPrice());
        }

        if (request.getCategory() != null) {
            menuItem.setCategory(request.getCategory());
        }

        if (request.getAvailable() != null) {
            menuItem.setAvailable(request.getAvailable());
        }

        MenuItem updatedItem = menuItemRepository.save(menuItem);
        return toMenuItemResponse(updatedItem);
    }

    @Override
    public void deleteMenuItem(String restaurantCode, String menuItemCode) {

        findRestaurantOrThrow(restaurantCode);

        MenuItem menuItem = menuItemRepository.findByMenuItemCode(menuItemCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu item not found: " + menuItemCode));

        if (!menuItem.getRestaurant().getRestaurantCode().equals(restaurantCode)) {
            throw new IllegalArgumentException("Menu item does not belong to this restaurant.");
        }

        menuItemRepository.delete(menuItem);
    }

    @Override
    public List<MenuItemResponse> getMenuByRestaurantCode(String restaurantCode) {

        findRestaurantOrThrow(restaurantCode);

        return menuItemRepository.findByRestaurantRestaurantCode(restaurantCode)
                .stream()
                .map(this::toMenuItemResponse)
                .toList();
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private Restaurant findRestaurantOrThrow(String restaurantCode) {
        return restaurantRepository.findByRestaurantCode(restaurantCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with code: " + restaurantCode));
    }

    private String generateRestaurantCode() {
        long rCount = restaurantRepository.count() + 1;
        return String.format("RST%03d", rCount);
    }

    private String generateMenuItemCode() {
        long iCount = menuItemRepository.count() + 1;
        return String.format("ITM%03d", iCount);
    }

    private RestaurantResponse toRestaurantResponse(Restaurant restaurant) {

        RestaurantResponse response = new RestaurantResponse();
        response.setRestaurantCode(restaurant.getRestaurantCode());
        response.setName(restaurant.getName());
        response.setDescription(restaurant.getDescription());
        response.setCuisine(restaurant.getCuisine());
        response.setOwnerName(restaurant.getOwnerName());
        response.setContactPhone(restaurant.getContactPhone());
        response.setContactEmail(restaurant.getContactEmail());
        response.setOpeningHours(restaurant.getOpeningHours());
        response.setStatus(restaurant.getStatus());
        response.setDateRegistered(restaurant.getDateRegistered());
        response.setAddress(restaurant.getAddress());

        // Include menu items in response
        List<MenuItemResponse> menuItems = restaurant.getMenuItems()
                .stream()
                .map(this::toMenuItemResponse)
                .toList();
        response.setMenuItems(menuItems);

        return response;
    }

    private MenuItemResponse toMenuItemResponse(MenuItem item) {

        MenuItemResponse response = new MenuItemResponse();
        response.setMenuItemCode(item.getMenuItemCode());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setPrice(item.getPrice());
        response.setCategory(item.getCategory());
        response.setAvailable(item.isAvailable());
        response.setRestaurantCode(item.getRestaurant().getRestaurantCode());

        return response;
    }
}
