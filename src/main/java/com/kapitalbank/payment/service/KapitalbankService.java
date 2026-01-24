package com.kapitalbank.payment.service;

import com.kapitalbank.payment.config.KapitalbankProperties;
import com.kapitalbank.payment.dao.entity.Order;
import com.kapitalbank.payment.dao.entity.User;
import com.kapitalbank.payment.dao.repo.OrderRepository;
import com.kapitalbank.payment.dao.repo.UserRepository;
import com.kapitalbank.payment.mapper.OrderMapper;
import com.kapitalbank.payment.model.dto.CreateOrderResponse;
import com.kapitalbank.payment.model.dto.CreatePaymentRequest;
import com.kapitalbank.payment.model.dto.EmailDto;
import com.kapitalbank.payment.model.dto.KapitalbankCallbackResult;
import com.kapitalbank.payment.model.dto.OrderResponse;
import com.kapitalbank.payment.model.enums.KapitalbankOrderType;
import com.kapitalbank.payment.model.enums.OrderStatus;
import com.kapitalbank.payment.model.exception.KapitalbankException;
import com.kapitalbank.payment.util.EmailUtil;
import com.kapitalbank.payment.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kapitalbank.payment.model.enums.LinkType.LICENSE;

@Slf4j
@Service
@RequiredArgsConstructor
public class KapitalbankService {

    private final KapitalbankProperties props;
    private final WebClient.Builder webClientBuilder;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserUtil userUtil;
    private final UserRepository userRepository;
    private final EmailUtil emailUtil;
    private final LicenseService licenseService;


    private String apiBaseUrl() {
        return props.apiBaseUrl();
    }

    public CreateOrderResponse createOrderAndGetPaymentUrl(
            CreatePaymentRequest request,
            HttpServletRequest httpServletRequest) {

        User currentUser = userUtil.getCurrentUser(httpServletRequest);

        Map<String, Object> data = Map.of(
                "amount", request.amount(),
                "description", request.description()
        );

        // 1️⃣ Bankda order yaradılır
        OrderResponse orderResponse = createPreAuthOrder(data);

        // 2️⃣ Local DB-də order saxlanılır
        orderRepository.save(orderMapper.buildOrder(
                props.getCurrency(),
                request.amount(),
                currentUser.getId(),
                orderResponse.id(),        // bankOrderId
                orderResponse.password()
        ));

        // 3️⃣ BANKIN verdiyi payment URL birbaşa qaytarılır
        return new CreateOrderResponse(
                orderResponse.id(),
                orderResponse.hppUrl()     // 🔴 ƏN VACİB DƏYİŞİKLİK
        );
    }


    public OrderResponse createPreAuthOrder(Map<String, Object> data) {
        return createOrderByType("Order_DMS", data);
    }


    protected OrderResponse createOrderByType(
            String typeRid,
            Map<String, Object> data) {

        Map<String, Object> order = new HashMap<>();

        order.put("typeRid", typeRid);
        order.put("amount", data.get("amount").toString());
        order.put("currency", props.getCurrency());
        order.put("language", props.getLanguage());
        order.put("description", data.getOrDefault("description", ""));
        order.put("callbackUrl", props.getRedirect().getCallback());
        order.put("hppRedirectUrl", props.getRedirect().getReturnUrl());

        if (data.containsKey("title")) {
            order.put("title", data.get("title"));
        }

        if (props.isSaveCards()
                || Boolean.TRUE.equals(data.get("save_card"))) {

            order.put("hppCofCapturePurposes",
                    List.of("UnspecifiedMit", "Cit", "Recurring"));

            order.put("aut", Map.of("purpose", "AddCard"));

            if (data.containsKey("stored_id")) {
                order.put("srcToken",
                        Map.of("storedId", data.get("stored_id")));
            }
        }

        Map<String, Object> payload = Map.of("order", order);
        Map<String, Object> response = request("POST", "/order", payload);

        Map<String, Object> r = cast(response.get("order"));

        return new OrderResponse(
                Long.valueOf(r.get("id").toString()),
                r.get("hppUrl").toString(),
                r.get("password").toString(),
                r.get("status").toString(),
                (String) r.get("secret"),
                (String) r.get("cvv2AuthStatus")
        );
    }

    /* =======================
       HTTP client
       ======================= */

    protected Map<String, Object> request(
            String method,
            String endpoint,
            Map<String, Object> body) {

        String url = apiBaseUrl() + endpoint;

        WebClient client = webClientBuilder
                .baseUrl(url)
                .defaultHeaders(h -> {
                    h.set("Merchant-Id", props.getApi().getMerchantId());
                    h.set("Terminal-Id", props.getApi().getTerminalId());
                })
                .build();

        try {
            return ("GET".equals(method)
                    ? client.get()
                    : client.post().bodyValue(body))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

        } catch (Exception ex) {
            throw new KapitalbankException(
                    "Kapitalbank API xətası: " + ex.getMessage());
        }
    }


    public ResponseEntity<Void> callback(HttpServletRequest request) {
        Map<String, String> params = extractParams(request);

        try {
            KapitalbankCallbackResult result = verifyCallback(params);

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

     /* =======================
       Callback verification
       ======================= */

    public KapitalbankCallbackResult verifyCallback(
            Map<String, String> params) {

        String orderId = params.getOrDefault("ID", params.get("id"));
        String callbackStatus = params.getOrDefault("STATUS", params.get("status"));

        if (orderId == null) {
            throw new KapitalbankException(
                    "Kapitalbank callback-də Order ID tapılmadı");
        }

        Map<String, Object> details = getOrderDetails(Long.valueOf(orderId), true);

        Map<String, Object> order = cast(details.get("order"));
        String actualStatus = order.getOrDefault("status", "Unknown").toString();

        Long storedTokenId = extractStoredTokenId(order);

        return new KapitalbankCallbackResult(
                Long.valueOf(orderId),
                callbackStatus,
                actualStatus,
                isSuccessful(KapitalbankOrderType.ORDER_SMS, actualStatus),
                order,
                storedTokenId
        );
    }

       /* =======================
       Order queries
       ======================= */

    public Map<String, Object> getOrderDetails(Long orderId, boolean full) {
        String query = full
                ? "?tranDetailLevel=2&tokenDetailLevel=2&orderDetailLevel=2"
                : "";
        return request("GET", "/order/" + orderId + query, Map.of());
    }


    private Long extractStoredTokenId(Map<String, Object> order) {
        Object tokens = order.get("storedTokens");
        if (tokens instanceof List<?> list && !list.isEmpty()) {
            Object first = list.getFirst();
            if (first instanceof Map<?, ?> token) {
                Object id = token.get("id");
                if (id != null) {
                    return Long.valueOf(id.toString());
                }
            }
        }
        return null;
    }


    /* =======================
       Status helpers
       ======================= */

    public boolean isSuccessful(
            KapitalbankOrderType type,
            String status) {

        return switch (type) {

            // Simple Sale → ONLY full payment is success
            case ORDER_SMS -> status.equals("FullyPaid");

            // PreAuth → Approved is success (money blocked)
            case ORDER_DMS -> status.equals("Approved")
                    || status.equals("FullyPaid");

            // Recurring → partial allowed
            case ORDER_REC -> status.equals("FullyPaid")
                    || status.equals("PartiallyPaid");
        };
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        return request.getParameterMap()
                .entrySet()
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue()[0]
                ));
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> cast(Object obj) {
        return (Map<String, Object>) obj;
    }

}

