package com.kapitalbank.payment.mapper;

import com.kapitalbank.payment.dao.entity.Order;
import com.kapitalbank.payment.model.dto.CreatePaymentRequest;
import com.kapitalbank.payment.model.enums.OrderStatus;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {


    default Order buildOrder(String currency, BigDecimal amount, CreatePaymentRequest createPaymentRequest, Long userId, Long bankOrderId, String bankPassword) {
        Order orderEntity = new Order();
        orderEntity.setUserId(userId);
        orderEntity.setAmount(amount);
        orderEntity.setCurrency(currency);
        orderEntity.setUserId(userId);
        orderEntity.setBankPassword(bankPassword);
        orderEntity.setStatus(OrderStatus.INITIAL);
        orderEntity.setProductType(createPaymentRequest.productType());
        orderEntity.setBankOrderId(bankOrderId);

        return orderEntity;
    }
}
