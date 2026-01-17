package com.kapitalbank.payment.model.event;

import java.util.List;
import java.util.Map;

public record PaymentSuccessfulEvent(
        Long orderId,
        String status,
        Map<String, Object> orderDetails,
        Long storedTokenId
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
     * Masked card number
     * (maps Laravel: orderDetails['srcToken']['displayName'])
     */
    @SuppressWarnings("unchecked")
    public String getCardMask() {
        Object srcToken = orderDetails.get("srcToken");
        if (srcToken instanceof Map<?, ?> tokenMap) {
            Object displayName = tokenMap.get("displayName");
            return displayName != null ? displayName.toString() : null;
        }
        return null;
    }

    /**
     * Transaction ID
     * (maps Laravel: orderDetails['trans'][0]['actionId'])
     */
    @SuppressWarnings("unchecked")
    public String getTransactionId() {
        Object trans = orderDetails.get("trans");
        if (trans instanceof List<?> transList && !transList.isEmpty()) {
            Object first = transList.get(0);
            if (first instanceof Map<?, ?> tx) {
                Object actionId = tx.get("actionId");
                return actionId != null ? actionId.toString() : null;
            }
        }
        return null;
    }

    /**
     * Approval code
     * (maps Laravel: orderDetails['trans'][0]['approvalCode'])
     */
    @SuppressWarnings("unchecked")
    public String getApprovalCode() {
        Object trans = orderDetails.get("trans");
        if (trans instanceof List<?> transList && !transList.isEmpty()) {
            Object first = transList.get(0);
            if (first instanceof Map<?, ?> tx) {
                Object approvalCode = tx.get("approvalCode");
                return approvalCode != null ? approvalCode.toString() : null;
            }
        }
        return null;
    }
}

