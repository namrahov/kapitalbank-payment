package com.kapitalbank.payment.model.exception;

public class UnavailableException extends RuntimeException {

    public UnavailableException(String message) {
        super(message);
    }

}
