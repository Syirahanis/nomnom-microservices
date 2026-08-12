package com.nomnom.restaurantservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// DDD Entity: MenuItem
// A MenuItem has its own identity (menuItemCode) and belongs to a
// Restaurant aggregate. It cannot exist without a parent Restaurant.
// Price is treated as a Money concept here using BigDecimal (MYR).
@Entity
@Table(name = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String menuItemCode;

    @Column(nullable = false)
    private String name;

    private String description;

    // Price in MYR — all monetary values are in Ringgit (per PID assumption)
    @Column(nullable = false)
    private BigDecimal price;

    // e.g. "Main Course", "Drinks", "Dessert"
    private String category;

    private boolean available;

    // MenuItem belongs to its Restaurant aggregate root
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Restaurant restaurant;
}
