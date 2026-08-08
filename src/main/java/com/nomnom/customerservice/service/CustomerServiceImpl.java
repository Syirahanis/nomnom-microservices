package com.nomnom.customerservice.service;

import com.nomnom.customerservice.dto.CustomerResponse;
import com.nomnom.customerservice.dto.RegisterCustomerRequest;
import com.nomnom.customerservice.dto.UpdateCustomerRequest;
import com.nomnom.customerservice.entity.AccountStatus;
import com.nomnom.customerservice.entity.Customer;
import com.nomnom.customerservice.exception.DuplicateResourceException;
import com.nomnom.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.nomnom.customerservice.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public CustomerResponse registerCustomer(RegisterCustomerRequest request) {

        // Business Rule 1:
        // At least one contact method must be provided.
        if ((request.getEmail() == null || request.getEmail().isBlank())
                && (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank())) {

            throw new IllegalArgumentException(
                    "Either email or phone number must be provided.");
        }

        // Business Rule 2:
        // Email must be unique.
        if (request.getEmail() != null && !request.getEmail().isBlank()) {

            Optional<Customer> existingCustomer =
                    customerRepository.findByEmail(request.getEmail());

            if (existingCustomer.isPresent()) {
                throw new DuplicateResourceException(
                        "Email already exists.");
            }
        }

        // Business Rule 3:
        // Phone number must be unique.
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {

            Optional<Customer> existingCustomer =
                    customerRepository.findByPhoneNumber(request.getPhoneNumber());

            if (existingCustomer.isPresent()) {
                throw new DuplicateResourceException(
                        "Phone number already exists.");
            }
        }

        // Create new Customer
        Customer customer = new Customer();

        customer.setCustomerCode(generateCustomerCode());
        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setPasswordHash(request.getPassword());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setDateRegistered(LocalDateTime.now());
        customer.setLastLogin(null);
        customer.setProfilePictureUrl(null);
        customer.setLoyaltyPoints(0);
        customer.setAccountStatus(AccountStatus.ACTIVE);
        customer.setAddress(request.getAddress());

        // Save to database
        Customer savedCustomer = customerRepository.save(customer);

        // Convert Customer to CustomerResponse
        CustomerResponse response = new CustomerResponse();

        response.setCustomerCode(savedCustomer.getCustomerCode());
        response.setFullName(savedCustomer.getFullName());
        response.setEmail(savedCustomer.getEmail());
        response.setPhoneNumber(savedCustomer.getPhoneNumber());
        response.setLoyaltyPoints(savedCustomer.getLoyaltyPoints());
        response.setAccountStatus(savedCustomer.getAccountStatus());
        response.setDateRegistered(savedCustomer.getDateRegistered());
        response.setAddress(savedCustomer.getAddress());

        return response;
    }

    private String generateCustomerCode() {
        return "CUS" + System.currentTimeMillis();
    }

    @Override
    public CustomerResponse getCustomerByCustomerCode(String customerCode) {

        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with code: " + customerCode));

        CustomerResponse response = new CustomerResponse();

        response.setCustomerCode(customer.getCustomerCode());
        response.setFullName(customer.getFullName());
        response.setEmail(customer.getEmail());
        response.setPhoneNumber(customer.getPhoneNumber());
        response.setLoyaltyPoints(customer.getLoyaltyPoints());
        response.setAccountStatus(customer.getAccountStatus());
        response.setDateRegistered(customer.getDateRegistered());
        response.setAddress(customer.getAddress());

        return response;
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {

        List<Customer> customers = customerRepository.findAll();

        return customers.stream()
                .map(customer -> {
                    CustomerResponse response = new CustomerResponse();

                    response.setCustomerCode(customer.getCustomerCode());
                    response.setFullName(customer.getFullName());
                    response.setEmail(customer.getEmail());
                    response.setPhoneNumber(customer.getPhoneNumber());
                    response.setLoyaltyPoints(customer.getLoyaltyPoints());
                    response.setAccountStatus(customer.getAccountStatus());
                    response.setDateRegistered(customer.getDateRegistered());
                    response.setAddress(customer.getAddress());

                    return response;
                })
                .toList();
    }

    @Override
    public CustomerResponse updateCustomer(
            String customerCode,
            UpdateCustomerRequest request) {

        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with code: " + customerCode));

        // Update full name
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            customer.setFullName(request.getFullName());
        }

        // Update email
        if (request.getEmail() != null && !request.getEmail().isBlank()) {

            if (!request.getEmail().equals(customer.getEmail())) {

                Optional<Customer> existingCustomer =
                        customerRepository.findByEmail(request.getEmail());

                if (existingCustomer.isPresent()) {
                    throw new DuplicateResourceException(
                            "Email already exists.");
                }
            }

            customer.setEmail(request.getEmail());
        }

        // Update phone number
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {

            if (!request.getPhoneNumber().equals(customer.getPhoneNumber())) {

                Optional<Customer> existingCustomer =
                        customerRepository.findByPhoneNumber(request.getPhoneNumber());

                if (existingCustomer.isPresent()) {
                    throw new DuplicateResourceException(
                            "Phone number already exists.");
                }
            }

            customer.setPhoneNumber(request.getPhoneNumber());
        }

        // Update date of birth
        if (request.getDateOfBirth() != null) {
            customer.setDateOfBirth(request.getDateOfBirth());
        }

        // Update address
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }

        Customer updatedCustomer = customerRepository.save(customer);

        CustomerResponse response = new CustomerResponse();

        response.setCustomerCode(updatedCustomer.getCustomerCode());
        response.setFullName(updatedCustomer.getFullName());
        response.setEmail(updatedCustomer.getEmail());
        response.setPhoneNumber(updatedCustomer.getPhoneNumber());
        response.setLoyaltyPoints(updatedCustomer.getLoyaltyPoints());
        response.setAccountStatus(updatedCustomer.getAccountStatus());
        response.setDateRegistered(updatedCustomer.getDateRegistered());
        response.setAddress(updatedCustomer.getAddress());

        return response;
    }

    @Override
    public void deleteCustomer(String customerCode) {

        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with code: " + customerCode));

        customer.setAccountStatus(AccountStatus.DELETED);

        customerRepository.save(customer);
    }
}