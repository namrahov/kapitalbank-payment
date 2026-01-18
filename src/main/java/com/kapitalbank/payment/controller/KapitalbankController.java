package com.kapitalbank.payment.controller;

import com.kapitalbank.payment.model.dto.CreateOrderResponse;
import com.kapitalbank.payment.model.dto.CreatePaymentRequest;
import com.kapitalbank.payment.model.dto.KapitalbankCallbackResult;
import com.kapitalbank.payment.model.event.PaymentFailedEvent;
import com.kapitalbank.payment.model.event.PaymentSuccessfulEvent;
import com.kapitalbank.payment.service.KapitalbankService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/payment/kapitalbank")
@Validated
public class KapitalbankController {

    private final KapitalbankService kapitalbankService;
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping
    public CreateOrderResponse createOrder(@RequestBody @Valid CreatePaymentRequest request) {
        return kapitalbankService.createOrderAndGetPaymentUrl(request);
    }










    /**
     * Kapitalbank callback endpoint
     * Supports BOTH GET and POST
     */
    @RequestMapping(
            value = "/callback",
            method = {RequestMethod.GET, RequestMethod.POST}
    )
    public String callback(
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Map<String, String> params = extractParams(request);
        log.info("Kapitalbank callback received: {}", params);

        try {
            KapitalbankCallbackResult result =
                    kapitalbankService.verifyCallback(params);

            if (result.successful()) {
                publishSuccess(result);
                populateSuccessRedirect(result, redirectAttributes);
                return "redirect:/payment/kapitalbank/success";
            }

            publishFailure(result);
            populateFailureRedirect(result, redirectAttributes);
            return "redirect:/payment/kapitalbank/error";

        } catch (Exception ex) {
            log.error("Kapitalbank callback verification failed", ex);

            redirectAttributes.addFlashAttribute("kapitalbank_success", false);
            redirectAttributes.addFlashAttribute(
                    "kapitalbank_error", ex.getMessage());

            return "redirect:/payment/kapitalbank/error";
        }
    }

    // =========================
    // Success page
    // =========================
    @RequestMapping("/success")
    public String success() {
        return "kapitalbank/success";
    }

    // =========================
    // Error page
    // =========================
    @RequestMapping("/error")
    public String error() {
        return "kapitalbank/error";
    }

    // =========================
    // Helpers
    // =========================

    private Map<String, String> extractParams(HttpServletRequest request) {
        return request.getParameterMap()
                .entrySet()
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue()[0]
                ));
    }

    private void publishSuccess(KapitalbankCallbackResult result) {
        eventPublisher.publishEvent(
                new PaymentSuccessfulEvent(
                        result.orderId(),
                        result.getStatus(),
                        result.orderDetails(),
                        result.storedTokenId()
                )
        );
    }

    private void publishFailure(KapitalbankCallbackResult result) {
        eventPublisher.publishEvent(
                new PaymentFailedEvent(
                        result.orderId(),
                        result.getStatus(),
                        result.orderDetails(),
                        result.getStatus(),
                        "Ödəniş uğursuz oldu: " + result.getStatus()
                )
        );
    }

    private void populateSuccessRedirect(
            KapitalbankCallbackResult result,
            RedirectAttributes attrs) {

        attrs.addFlashAttribute("kapitalbank_order_id", result.orderId());
        attrs.addFlashAttribute("kapitalbank_status", result.getStatus());
        attrs.addFlashAttribute("kapitalbank_success", true);
    }

    private void populateFailureRedirect(
            KapitalbankCallbackResult result,
            RedirectAttributes attrs) {

        attrs.addFlashAttribute("kapitalbank_order_id", result.orderId());
        attrs.addFlashAttribute("kapitalbank_status", result.getStatus());
        attrs.addFlashAttribute("kapitalbank_success", false);
        attrs.addFlashAttribute(
                "kapitalbank_error", "Ödəniş uğursuz oldu");
    }
}


