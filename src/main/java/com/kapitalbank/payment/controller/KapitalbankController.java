package com.kapitalbank.payment.controller;

import com.kapitalbank.payment.model.dto.KapitalbankCallbackResult;
import com.kapitalbank.payment.model.event.PaymentFailedEvent;
import com.kapitalbank.payment.model.event.PaymentSuccessfulEvent;
import com.kapitalbank.payment.service.KapitalbankService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/payment/kapitalbank")
public class KapitalbankController {

    private final KapitalbankService kapitalbankService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Kapitalbank callback endpoint
     */
    @GetMapping("/callback")
    public String callback(
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Map<String, String> params = request.getParameterMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue()[0] // take first value
                ));
        log.info("Kapitalbank Callback: {}", params);

        try {
            KapitalbankCallbackResult result =
                    kapitalbankService.verifyCallback(params);

            if (result.successful()) {

                eventPublisher.publishEvent(
                        new PaymentSuccessfulEvent(
                                result.orderId(),
                                result.getStatus(),
                                result.orderDetails(),
                                result.storedTokenId()
                        )
                );

                redirectAttributes.addFlashAttribute(
                        "kapitalbank_order_id", result.orderId());
                redirectAttributes.addFlashAttribute(
                        "kapitalbank_status", result.getStatus());
                redirectAttributes.addFlashAttribute(
                        "kapitalbank_success", true);

                return "redirect:/payment/kapitalbank/success";

            } else {

                eventPublisher.publishEvent(
                        new PaymentFailedEvent(
                                result.orderId(),
                                result.getStatus(),
                                result.orderDetails(),
                                result.getStatus(),
                                "Ödəniş uğursuz oldu: " + result.getStatus()
                        )
                );

                redirectAttributes.addFlashAttribute(
                        "kapitalbank_order_id", result.orderId());
                redirectAttributes.addFlashAttribute(
                        "kapitalbank_status", result.getStatus());
                redirectAttributes.addFlashAttribute(
                        "kapitalbank_success", false);
                redirectAttributes.addFlashAttribute(
                        "kapitalbank_error", "Ödəniş uğursuz oldu");

                return "redirect:/payment/kapitalbank/error";
            }

        } catch (Exception ex) {
            log.error("Kapitalbank Callback Exception", ex);

            redirectAttributes.addFlashAttribute(
                    "kapitalbank_success", false);
            redirectAttributes.addFlashAttribute(
                    "kapitalbank_error", ex.getMessage());

            return "redirect:/payment/kapitalbank/error";
        }
    }

    /**
     * Success page
     */
    @GetMapping("/success")
    public String success() {
        return "kapitalbank/success";
    }

    /**
     * Error page
     */
    @GetMapping("/error")
    public String error() {
        return "kapitalbank/error";
    }
}

