package com.kapitalbank.payment.service;

import com.kapitalbank.payment.config.KapitalbankProperties;
import com.kapitalbank.payment.dao.entity.KapitalbankPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KapitalbankPaymentService {

    private final KapitalbankProperties props;

    public String getPaymentUrl(KapitalbankPayment payment) {

        String mode = props.getMode();
        String hppUrl = props.getHppUrl().get(mode);

        return hppUrl +
                "?id=" + payment.getOrderId() +
                "&password=" + payment.getPassword();
    }
}

