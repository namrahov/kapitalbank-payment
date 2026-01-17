package com.kapitalbank.payment.model.event;

import java.util.Map;

public record PaymentFailedEvent(
        Long orderId,
        String status,
        Map<String, Object> orderDetails,
        String errorCode,
        String errorMessage
) {

    /**
     * Payment amount
     */
    public double getAmount() {
        Object amount = orderDetails.get("amount");
        if (amount == null) {
            return 0.0;
        }
        return Double.parseDouble(amount.toString());
    }

    /**
     * Currency (default AZN)
     */
    public String getCurrency() {
        Object currency = orderDetails.get("currency");
        return currency != null ? currency.toString() : "AZN";
    }

    /**
     * Decline reason
     */
    public String getDeclineReason() {
        return errorCode;
    }
}

