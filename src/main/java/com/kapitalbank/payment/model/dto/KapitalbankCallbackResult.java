package com.kapitalbank.payment.model.dto;


import java.util.Map;

public record KapitalbankCallbackResult(
        Long orderId,
        String callbackStatus,
        String actualStatus,
        boolean successful,
        Map<String, Object> orderDetails,
        Long storedTokenId) {
    public String getStatus() {
        return actualStatus;
    }
}

