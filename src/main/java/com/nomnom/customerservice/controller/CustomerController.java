package com.nomnom.customerservice.controller;

import com.nomnom.customerservice.dto.CustomerResponse;
import com.nomnom.customerservice.dto.RegisterCustomerRequest;
import com.nomnom.customerservice.dto.UpdateCustomerRequest;
import com.nomnom.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // Register a new customer
    @PostMapping
    public ResponseEntity<CustomerResponse> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request) {

        CustomerResponse response =
                customerService.registerCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all customers
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {

        return ResponseEntity.ok(
                customerService.getAllCustomers()
        );
    }

    // Get customer by customer code
    @GetMapping("/{customerCode}")
    public ResponseEntity<CustomerResponse> getCustomerByCustomerCode(
            @PathVariable String customerCode) {

        return ResponseEntity.ok(
                customerService.getCustomerByCustomerCode(customerCode)
        );
    }

    // Update customer
    @PutMapping("/{customerCode}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable String customerCode,
            @Valid @RequestBody UpdateCustomerRequest request) {

        return ResponseEntity.ok(
                customerService.updateCustomer(customerCode, request)
        );
    }

    // Delete customer
    @DeleteMapping("/{customerCode}")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable String customerCode) {

        customerService.deleteCustomer(customerCode);

        return ResponseEntity.noContent().build();
    }
}