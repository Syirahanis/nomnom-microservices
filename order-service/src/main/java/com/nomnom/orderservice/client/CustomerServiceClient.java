package com.nomnom.orderservice.client;

import com.nomnom.orderservice.exception.CustomerNotFoundException;
import com.nomnom.orderservice.exception.DownstreamServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Synchronous, REST-based communication with Customer Service - this is
 * the "REST requests" half of NomNom's inter-service communication
 * (Kafka handles the asynchronous half). Calls the customer-lookup
 * endpoint that Insyirah's Customer Service already exposes.
 */
@Component
public class CustomerServiceClient {

    private final RestTemplate restTemplate;
    private final String customerServiceBaseUrl;

    public CustomerServiceClient(
            RestTemplate restTemplate,
            @Value("${customer-service.base-url}") String customerServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.customerServiceBaseUrl = customerServiceBaseUrl;
    }

    /**
     * Verifies that a customerCode exists (and is an active account) in
     * Customer Service before an order is allowed to be placed against it.
     */
    public void verifyCustomerExists(String customerCode) {

        String url = customerServiceBaseUrl + "/customers/" + customerCode;

        try {
            CustomerLookupResponse customer =
                    restTemplate.getForObject(url, CustomerLookupResponse.class);

            if (customer == null) {
                throw new CustomerNotFoundException(
                        "Customer not found: " + customerCode);
            }

            if ("DELETED".equals(customer.getAccountStatus())
                    || "INACTIVE".equals(customer.getAccountStatus())) {
                throw new CustomerNotFoundException(
                        "Customer account is not active: " + customerCode);
            }

        } catch (HttpClientErrorException.NotFound ex) {
            throw new CustomerNotFoundException(
                    "Customer not found: " + customerCode);

        } catch (RestClientException ex) {
            throw new DownstreamServiceException(
                    "Could not reach Customer Service to verify customer "
                            + customerCode + ": " + ex.getMessage());
        }
    }
}
