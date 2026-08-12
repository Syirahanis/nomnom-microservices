package com.nomnom.deliveryservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deliveryCode;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private String customerCode;

    @Column(nullable = false)
    private String riderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(nullable = false)
    private String deliveryAddress;

    private LocalDateTime assignedAt;

    private LocalDateTime inTransitAt;

    private LocalDateTime deliveredAt;

    private Delivery(String deliveryCode, String orderId, String customerCode, String riderId, String deliveryAddress) {
        this.deliveryCode = deliveryCode;
        this.orderId = orderId;
        this.customerCode = customerCode;
        this.riderId = riderId;
        this.deliveryAddress = deliveryAddress;
        this.status = DeliveryStatus.ASSIGNED;
        this.assignedAt = LocalDateTime.now();
    }

    public static Delivery assign(String deliveryCode, String orderId, String customerCode, String riderId, String deliveryAddress) {
        return new Delivery(deliveryCode, orderId, customerCode, riderId, deliveryAddress);
    }

    public void startTransit() {
        if (this.status != DeliveryStatus.ASSIGNED) {
            throw new IllegalStateException(
                    "Only an ASSIGNED delivery can start transit (current status: " + status + ")");
        }
        this.status = DeliveryStatus.IN_TRANSIT;
        this.inTransitAt = LocalDateTime.now();
    }

    public void complete() {
        if (this.status != DeliveryStatus.IN_TRANSIT) {
            throw new IllegalStateException(
                    "Only an IN_TRANSIT delivery can be completed (current status: " + status + ")");
        }
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }
}
