package com.nomnom.deliveryservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "riders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Rider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String riderCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiderStatus status;

    private LocalDateTime registeredAt;

    private Rider(String riderCode, String name, String phoneNumber) {
        this.riderCode = riderCode;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.status = RiderStatus.AVAILABLE;
        this.registeredAt = LocalDateTime.now();
    }

    public static Rider register(String riderCode, String name, String phoneNumber) {
        return new Rider(riderCode, name, phoneNumber);
    }

    public void assign() {
        if (this.status != RiderStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Rider " + riderCode + " is not available (current status: " + status + ")");
        }
        this.status = RiderStatus.BUSY;
    }

    public void release() {
        if (this.status != RiderStatus.BUSY) {
            throw new IllegalStateException(
                    "Rider " + riderCode + " is not busy (current status: " + status + ")");
        }
        this.status = RiderStatus.AVAILABLE;
    }
}
