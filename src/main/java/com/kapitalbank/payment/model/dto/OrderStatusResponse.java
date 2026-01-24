package com.kapitalbank.payment.model.dto;

import com.kapitalbank.payment.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusResponse {
    private OrderStatus orderStatus;
}
