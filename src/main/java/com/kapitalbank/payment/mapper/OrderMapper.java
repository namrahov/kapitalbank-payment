package com.kapitalbank.payment.mapper;

import com.kapitalbank.payment.dao.entity.Order;
import com.kapitalbank.payment.model.enums.OrderStatus;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {


    default Order buildOrder(String currency, BigDecimal amount, Long userId, Long bankOrderId) {
        Order orderEntity = new Order();
        orderEntity.setUserId(userId);
        orderEntity.setAmount(amount);
        orderEntity.setCurrency(currency);
        orderEntity.setUserId(userId);
        orderEntity.setStatus(OrderStatus.INITIAL);
        orderEntity.setBankOrderId(bankOrderId);

        return orderEntity;
    }
}
