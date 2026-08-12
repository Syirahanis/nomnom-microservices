package com.nomnom.orderservice.client;

import lombok.Data;

/**
 * Mirrors only the fields Order Service actually needs from Customer
 * Service's GET /customers/{customerCode} response. Kept separate from
 * Customer Service's own CustomerResponse class on purpose - each
 * microservice owns its own view of data crossing a bounded context
 * boundary rather than sharing a DTO module.
 */
@Data
public class CustomerLookupResponse {

    private String customerCode;
    private String fullName;
    private String accountStatus;

}
