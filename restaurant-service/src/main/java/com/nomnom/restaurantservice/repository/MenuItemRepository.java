package com.nomnom.restaurantservice.repository;

import com.nomnom.restaurantservice.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    Optional<MenuItem> findByMenuItemCode(String menuItemCode);

    List<MenuItem> findByRestaurantRestaurantCode(String restaurantCode);

    List<MenuItem> findByRestaurantRestaurantCodeAndAvailableTrue(String restaurantCode);
}
