package com.nomnom.deliveryservice.dto;

import com.nomnom.deliveryservice.entity.RiderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RiderResponse {

    private Long id;

    private String riderCode;

    private String name;

    private String phoneNumber;

    private RiderStatus status;

    private LocalDateTime registeredAt;
}
