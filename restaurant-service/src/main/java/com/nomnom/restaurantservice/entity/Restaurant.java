package com.nomnom.restaurantservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// DDD Aggregate Root: Restaurant
// The Restaurant is the root of the Restaurant & Catalog bounded context.
// All access to MenuItems must go through Restaurant to maintain consistency.
// For example, you cannot add a MenuItem without a valid Restaurant.
@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Business identity — used across services instead of the DB id
    @Column(nullable = false, unique = true)
    private String restaurantCode;

    @Column(nullable = false)
    private String name;

    private String description;

    // e.g. "Malaysian", "Chinese", "Western"
    private String cuisine;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false, unique = true)
    private String contactPhone;

    @Column(unique = true)
    private String contactEmail;

    // Opening hours stored as simple string for prototype simplicity
    // e.g. "Mon-Fri 10:00-22:00"
    private String openingHours;

    @Enumerated(EnumType.STRING)
    private RestaurantStatus status;

    private LocalDateTime dateRegistered;

    // DDD Value Object embedded directly into the aggregate
    @Embedded
    private RestaurantAddress address;

    // DDD: Aggregate owns its child entities (MenuItems).
    // CascadeType.ALL means saving/deleting a Restaurant also
    // saves/deletes its MenuItems — the aggregate enforces lifecycle.
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuItem> menuItems = new ArrayList<>();
}
