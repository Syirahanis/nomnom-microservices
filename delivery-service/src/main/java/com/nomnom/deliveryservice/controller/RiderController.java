package com.nomnom.deliveryservice.controller;

import com.nomnom.deliveryservice.dto.RegisterRiderRequest;
import com.nomnom.deliveryservice.dto.RiderResponse;
import com.nomnom.deliveryservice.service.RiderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/riders")
@RequiredArgsConstructor
public class RiderController {

    private final RiderService riderService;

    // Register a new rider
    @PostMapping
    public ResponseEntity<RiderResponse> registerRider(
            @Valid @RequestBody RegisterRiderRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(riderService.registerRider(request));
    }

    // Get all riders
    @GetMapping
    public ResponseEntity<List<RiderResponse>> getAllRiders() {
        return ResponseEntity.ok(riderService.getAllRiders());
    }

    // Get rider by rider code
    @GetMapping("/{riderCode}")
    public ResponseEntity<RiderResponse> getRiderByCode(
            @PathVariable String riderCode) {
        return ResponseEntity.ok(riderService.getRiderByCode(riderCode));
    }
}
