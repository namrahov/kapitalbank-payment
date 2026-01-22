package com.kapitalbank.payment.model.exception;

public class RateLimitExceededException extends RuntimeException{

    public RateLimitExceededException(String message) {
        super(message);
    }

}
