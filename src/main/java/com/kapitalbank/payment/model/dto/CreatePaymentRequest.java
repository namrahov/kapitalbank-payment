package com.kapitalbank.payment.model.dto;


import com.kapitalbank.payment.model.enums.ProductType;

public record CreatePaymentRequest(
        ProductType productType,
        String description
) {}

