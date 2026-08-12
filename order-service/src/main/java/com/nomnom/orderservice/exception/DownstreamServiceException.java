package com.nomnom.orderservice.exception;

/** Raised when a synchronous call to another microservice (e.g. Customer Service) fails. */
public class DownstreamServiceException extends RuntimeException {

    public DownstreamServiceException(String message) {
        super(message);
    }

}
