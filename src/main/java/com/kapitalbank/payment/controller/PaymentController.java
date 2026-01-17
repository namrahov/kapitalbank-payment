package com.kapitalbank.payment.controller;

import com.kapitalbank.payment.client.KapitalbankClient;
import com.kapitalbank.payment.model.dto.CreatePaymentResponse;
import com.kapitalbank.payment.model.dto.PaymentRequest;
import com.kapitalbank.payment.model.dto.PaymentStatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final KapitalbankClient kapitalbankClient;

    /**
     * React calls this to start payment
     */
    @PostMapping
    public CreatePaymentResponse createPayment(@RequestBody @Valid PaymentRequest request) {

        var order = kapitalbankClient.createOrder(request.amount());

        return new CreatePaymentResponse(
                order.orderId(),
                order.redirectUrl()
        );
    }

    /**
     * React checks payment result
     */
    @GetMapping("/{orderId}")
    public PaymentStatusResponse getStatus(
            @PathVariable String orderId) {

        // read from DB
        return new PaymentStatusResponse(orderId, "PAID");
    }
}



