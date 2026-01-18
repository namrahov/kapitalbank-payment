package com.kapitalbank.payment.controller;

import com.kapitalbank.payment.model.dto.CreateOrderResponse;
import com.kapitalbank.payment.model.dto.CreatePaymentRequest;
import com.kapitalbank.payment.model.dto.KapitalbankCallbackResult;
import com.kapitalbank.payment.service.KapitalbankService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
    public ResponseEntity<Void> callback(HttpServletRequest request) {

        Map<String, String> params = extractParams(request);
        log.info("Kapitalbank callback received: {}", params);

        try {
            KapitalbankCallbackResult result =
                    kapitalbankService.verifyCallback(params);

            if (result.successful()) {

            } else {

            }

            // BANK only needs HTTP 200
            return ResponseEntity.ok().build();

        } catch (Exception ex) {
            log.error("Kapitalbank callback verification failed", ex);

            // Important: still return 200 to avoid retries storm
            return ResponseEntity.ok().build();
        }
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


}


