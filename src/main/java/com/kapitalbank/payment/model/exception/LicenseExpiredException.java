package com.kapitalbank.payment.model.exception;

public class LicenseExpiredException extends RuntimeException {
    public LicenseExpiredException(String message) { super(message); }
}

