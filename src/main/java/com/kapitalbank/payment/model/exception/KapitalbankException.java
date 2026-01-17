package com.kapitalbank.payment.model.exception;

public class KapitalbankException extends RuntimeException {

    private final String errorCode;
    private final Object errorDetails;

    public KapitalbankException(String message) {
        this(message, "UNKNOWN", null);
    }

    public KapitalbankException(String message, String errorCode, Object errorDetails) {
        super(message);
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
    }
}
