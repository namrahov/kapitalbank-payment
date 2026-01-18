package com.kapitalbank.payment.model.dto;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        BigDecimal amount,
        String description
) {}

