package com.kapitalbank.payment.controller;

import com.kapitalbank.payment.client.KapitalbankClient;
import com.kapitalbank.payment.model.dto.PaymentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final KapitalbankClient kapitalbankClient;

    // =========================
    // Checkout page
    // GET /checkout
    // =========================
    @GetMapping("/checkout")
    public String checkout() {
        return "payment/checkout";
    }

    // =========================
    // Start payment
    // POST /payment/pay
    // =========================
    @PostMapping("/payment/pay")
    public String pay(
            @Valid PaymentRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            var order = kapitalbankClient.createOrder(request.amount());
            return "redirect:" + order.redirectUrl();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "error", "Xəta baş verdi: " + e.getMessage()
            );
            return "redirect:/checkout";
        }
    }

    // =========================
    // Success page
    // GET /payment/success
    // =========================
    @GetMapping("/payment/success")
    public String success() {
        return "payment/success";
    }

    // =========================
    // Error page
    // GET /payment/error
    // =========================
    @GetMapping("/payment/error")
    public String error() {
        return "payment/error";
    }
}


