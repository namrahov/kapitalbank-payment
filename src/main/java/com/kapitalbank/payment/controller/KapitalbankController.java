package com.kapitalbank.payment.controller;

import com.kapitalbank.payment.dao.entity.Order;
import com.kapitalbank.payment.dao.entity.User;
import com.kapitalbank.payment.dao.repo.OrderRepository;
import com.kapitalbank.payment.dao.repo.UserRepository;
import com.kapitalbank.payment.model.dto.CreateOrderResponse;
import com.kapitalbank.payment.model.dto.CreatePaymentRequest;
import com.kapitalbank.payment.model.dto.EmailDto;
import com.kapitalbank.payment.model.dto.KapitalbankCallbackResult;
import com.kapitalbank.payment.model.dto.OrderResponse;
import com.kapitalbank.payment.model.enums.OrderStatus;
import com.kapitalbank.payment.service.KapitalbankService;
import com.kapitalbank.payment.service.LicenseService;
import com.kapitalbank.payment.util.EmailUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;
import java.util.Optional;

import static com.kapitalbank.payment.model.enums.LinkType.LICENSE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/payment/kapitalbank")
@Validated
public class KapitalbankController {

    private final KapitalbankService kapitalbankService;
    private final LicenseService licenseService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EmailUtil emailUtil;

    @PostMapping
    public CreateOrderResponse createOrder(@RequestBody @Valid CreatePaymentRequest request,
                                           HttpServletRequest httpServletRequest) {
        return kapitalbankService.createOrderAndGetPaymentUrl(request, httpServletRequest);
    }

    /**
     * Kapitalbank callback endpoint
     * Supports BOTH GET and POST
     */
    @RequestMapping(value="/callback", method={GET, POST})
    public ResponseEntity<Void> callback(HttpServletRequest request) {
        Map<String, String> params = extractParams(request);

        try {
            KapitalbankCallbackResult result = kapitalbankService.verifyCallback(params);

            Long bankOrderId = result.orderId();

            Order order = orderRepository.findByBankOrderId(bankOrderId)
                    .orElseThrow(() -> new IllegalStateException("Local order not found for bankOrderId=" + bankOrderId));

            // idempotency
            if (order.getStatus() == OrderStatus.SUCCESS) {
                return ResponseEntity.ok().build();
            }

            if (result.successful()) {
                User user = userRepository.findById(order.getUserId())
                        .orElseThrow(() -> new IllegalStateException("User not found"));

                String licenseKey = licenseService.generateLicense("1234fhfg");
                EmailDto emailDto = emailUtil.generateActivationEmail(licenseKey, LICENSE);
                emailUtil.send(emailDto.getFrom(), user.getEmail(), emailDto.getSubject(), emailDto.getBody());

                order.setStatus(OrderStatus.SUCCESS);
            } else {
                order.setStatus(OrderStatus.FAIL);
            }

            orderRepository.save(order);
            return ResponseEntity.ok().build();

        } catch (Exception ex) {
            log.error("Kapitalbank callback verification failed", ex);
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


