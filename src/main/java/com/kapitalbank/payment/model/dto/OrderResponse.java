package com.kapitalbank.payment.model.dto;

public record OrderResponse(
        Long id,
        String hppUrl,
        String password,
        String status,
        String secret,
        String cvv2AuthStatus
) {}

