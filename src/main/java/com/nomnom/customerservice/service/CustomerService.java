package com.nomnom.customerservice.service;

import com.nomnom.customerservice.dto.CustomerResponse;
import com.nomnom.customerservice.dto.RegisterCustomerRequest;
import com.nomnom.customerservice.dto.UpdateCustomerRequest;

import java.util.List;

public interface CustomerService {

    CustomerResponse registerCustomer(RegisterCustomerRequest request);

    CustomerResponse getCustomerByCustomerCode(String customerCode);

    List<CustomerResponse> getAllCustomers();

    CustomerResponse updateCustomer(
            String customerCode,
            UpdateCustomerRequest request);

    void deleteCustomer(String customerCode);

}