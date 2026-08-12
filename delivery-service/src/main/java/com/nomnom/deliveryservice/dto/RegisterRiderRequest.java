package com.nomnom.deliveryservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRiderRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "phoneNumber is required")
    private String phoneNumber;
}
