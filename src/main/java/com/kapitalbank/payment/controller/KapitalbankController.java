package com.kapitalbank.payment.controller;

import com.kapitalbank.payment.dao.entity.Order;
import com.kapitalbank.payment.dao.repo.OrderRepository;
import com.kapitalbank.payment.model.dto.CreateOrderResponse;
import com.kapitalbank.payment.model.dto.CreatePaymentRequest;
import com.kapitalbank.payment.model.dto.OrderStatusResponse;
import com.kapitalbank.payment.service.KapitalbankService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/payment/kapitalbank")
@Validated
public class KapitalbankController {

    private final KapitalbankService kapitalbankService;
    private final OrderRepository orderRepository;

    @PostMapping
    public CreateOrderResponse createOrder(@RequestBody @Valid CreatePaymentRequest request,
                                           HttpServletRequest httpServletRequest) {
        return kapitalbankService.createOrderAndGetPaymentUrl(request, httpServletRequest);
    }

    /**
     * Kapitalbank callback endpoint
     * Supports BOTH GET and POST
     */
    @RequestMapping(value = "/callback", method = {GET, POST})
    public ResponseEntity<Void> callback(HttpServletRequest request) {
        return kapitalbankService.callback(request);
    }

    @GetMapping("/orders/status")
    public OrderStatusResponse getStatus(@RequestParam Long bankOrderId) {
        Order order = orderRepository.findByBankOrderId(bankOrderId)
                .orElseThrow();

        return new OrderStatusResponse(order.getStatus());
    }

}


