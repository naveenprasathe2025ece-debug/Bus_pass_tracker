package com.buspass.exception;

/** Thrown when a request violates a business rule (HTTP 409). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
