package com.kapitalbank.payment.client;

import com.kapitalbank.payment.config.KapitalbankProperties;
import com.kapitalbank.payment.model.dto.KapitalOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KapitalbankClient {

    private final KapitalbankProperties properties;

    /**
     * Create payment order and return HPP redirect info
     */
    public KapitalOrder createOrder(BigDecimal amount) {

        String orderId = UUID.randomUUID().toString();

        String redirectUrl = buildHppRedirectUrl(orderId, amount);

        return new KapitalOrder(orderId, redirectUrl);
    }

    /**
     * Verify callback from Kapitalbank
     */
    public boolean verifyCallback(String orderId, String status, String signature) {
        // TODO: verify signature using properties.getApi().getSecretKey()
        return "SUCCESS".equalsIgnoreCase(status);
    }

    /**
     * Build HPP redirect URL
     */
    private String buildHppRedirectUrl(String orderId, BigDecimal amount) {
        return properties.hppUrl()
                + "?orderId=" + orderId
                + "&amount=" + amount
                + "&currency=" + properties.getHpp().getCurrency()
                + "&lang=" + properties.getHpp().getLanguage();
    }
}


