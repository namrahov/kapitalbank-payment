package com.kapitalbank.payment.model.dto;

public record CreateOrderResponse(
        Long orderId,
        String paymentUrl
) {
}
