package com.kapitalbank.payment.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull
        @DecimalMin("0.1")
        BigDecimal amount
) {}

