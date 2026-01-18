package com.kapitalbank.payment.controller;

import com.kapitalbank.payment.client.PaddleClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final PaddleClient paddleClient;

    public BillingController(PaddleClient paddleClient) {
        this.paddleClient = paddleClient;
    }

    @PostMapping("/checkout")
    public Map<String, Object> createCheckout(@RequestBody CheckoutRequest req) {
        Map<String, Object> body = buildTransaction(req.priceId(), req.email(), req.instanceId());
        Map<String, Object> resp = paddleClient.createTransaction(body);

        Map<String, Object> data = (Map<String, Object>) resp.get("data");
        Map<String, Object> checkout = (Map<String, Object>) data.get("checkout");

        return Map.of(
                "transactionId", data.get("id"),
                "checkoutUrl", checkout != null ? checkout.get("url") : null
        );
    }

    private Map<String, Object> buildTransaction(String priceId, String email, String instanceId) {
        Map<String, Object> item = Map.of("quantity", 1, "price_id", priceId);
        Map<String, Object> customData = Map.of("email", email, "instanceId", instanceId);
        return Map.of("items", List.of(item), "custom_data", customData);
    }

    public record CheckoutRequest(String priceId, String email, String instanceId) {}
}

