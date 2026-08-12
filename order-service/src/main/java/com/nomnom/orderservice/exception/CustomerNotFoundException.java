package com.nomnom.orderservice.exception;

/** Raised when the customerCode on a PlaceOrderRequest does not exist in Customer Service. */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String message) {
        super(message);
    }

}
