package com.nomnom.restaurantservice.repository;

import com.nomnom.restaurantservice.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// DDD Repository: RestaurantRepository
// Provides persistence operations for the Restaurant aggregate root.
// Following DDD, only aggregate roots have repositories — MenuItems
// are accessed through the Restaurant, not directly.
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findByRestaurantCode(String restaurantCode);

    boolean existsByContactPhone(String contactPhone);

    boolean existsByContactEmail(String contactEmail);
}
