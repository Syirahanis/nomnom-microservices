package com.nomnom.deliveryservice.controller;

import com.nomnom.deliveryservice.dto.DeliveryResponse;
import com.nomnom.deliveryservice.dto.DeliveryStatusResponse;
import com.nomnom.deliveryservice.dto.UpdateDeliveryStatusRequest;
import com.nomnom.deliveryservice.service.DeliveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    // List all deliveries
    @GetMapping
    public ResponseEntity<List<DeliveryResponse>> getAllDeliveries() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    // Get full delivery details by delivery code
    @GetMapping("/{deliveryCode}")
    public ResponseEntity<DeliveryResponse> getDeliveryByCode(
            @PathVariable String deliveryCode) {
        return ResponseEntity.ok(deliveryService.getDeliveryByCode(deliveryCode));
    }

    // Get delivery by order id
    @GetMapping("/order/{orderId}")
    public ResponseEntity<DeliveryResponse> getDeliveryByOrderId(
            @PathVariable String orderId) {
        return ResponseEntity.ok(deliveryService.getDeliveryByOrderId(orderId));
    }

    // Get current delivery status (tracking)
    @GetMapping("/{deliveryCode}/status")
    public ResponseEntity<DeliveryStatusResponse> getDeliveryStatus(
            @PathVariable String deliveryCode) {
        return ResponseEntity.ok(deliveryService.getDeliveryStatus(deliveryCode));
    }

    // Manually update delivery status (useful for testing/demo)
    @PutMapping("/{deliveryCode}/status")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(
            @PathVariable String deliveryCode,
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        return ResponseEntity.ok(deliveryService.updateDeliveryStatus(deliveryCode, request));
    }
}
