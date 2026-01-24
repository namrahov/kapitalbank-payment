package com.kapitalbank.payment.service;

import com.kapitalbank.payment.config.KapitalbankProperties;
import com.kapitalbank.payment.dao.entity.Order;
import com.kapitalbank.payment.dao.entity.User;
import com.kapitalbank.payment.dao.repo.OrderRepository;
import com.kapitalbank.payment.dao.repo.UserRepository;
import com.kapitalbank.payment.mapper.OrderMapper;
import com.kapitalbank.payment.model.dto.CreateOrderResponse;
import com.kapitalbank.payment.model.dto.CreatePaymentRequest;
import com.kapitalbank.payment.model.dto.KapitalbankCallbackResult;
import com.kapitalbank.payment.model.dto.OrderResponse;
import com.kapitalbank.payment.model.enums.KapitalbankOrderType;
import com.kapitalbank.payment.model.exception.KapitalbankException;
import com.kapitalbank.payment.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class KapitalbankService {

    private final KapitalbankProperties props;
    private final WebClient.Builder webClientBuilder;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserUtil userUtil;

    /* =======================
       URL helpers
       ======================= */

    private String apiBaseUrl() {
        return props.apiBaseUrl();
    }

    private String hppBaseUrl() {
        return props.hppUrl();
    }


    public CreateOrderResponse createOrderAndGetPaymentUrl(CreatePaymentRequest request,
                                                           HttpServletRequest httpServletRequest) {
        User currentUser = userUtil.getCurrentUser(httpServletRequest);

        Map<String, Object> data = Map.of(
                "amount", request.amount(),
                "description", request.description()
        );

        OrderResponse orderResponse = createPreAuthOrder(data);

        orderRepository.save(orderMapper.buildOrder(
                props.getHpp().getCurrency(),
                request.amount(),
                currentUser.getId(),
                orderResponse.id(),
                orderResponse.password()));

        String redirectUrl = getPaymentUrl(orderResponse.id(), orderResponse.password());

        return new CreateOrderResponse(orderResponse.id(), redirectUrl);

    }

    public OrderResponse createPreAuthOrder(Map<String, Object> data) {
        return createOrderByType("Order_DMS", data);
    }

    /* =======================
       Order creation
       ======================= */

    protected OrderResponse createOrderByType(
            String typeRid,
            Map<String, Object> data) {

        Map<String, Object> order = new HashMap<>();

        order.put("typeRid", typeRid);
        order.put("amount", data.get("amount").toString());
        order.put("currency", props.getHpp().getCurrency());
        order.put("language", props.getHpp().getLanguage());
        order.put("description", data.getOrDefault("description", ""));
        order.put("callbackUrl", props.getRedirect().getCallback());
        order.put("hppRedirectUrl", props.getRedirect().getReturnUrl());

        if (data.containsKey("title")) {
            order.put("title", data.get("title"));
        }

        if (props.getHpp().isSaveCards()
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
       HPP redirect
       ======================= */

    public String getPaymentUrl(Long orderId, String password) {
        return hppBaseUrl()
                + "?id=" + orderId
                + "&password=" + password;
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

    public String getOrderStatus(Long orderId) {
        Map<String, Object> details = getOrderDetails(orderId, false);
        Map<String, Object> order = cast(details.get("order"));
        return order != null
                ? order.getOrDefault("status", "Unknown").toString()
                : "Unknown";
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

    /* =======================
       Helpers
       ======================= */

    @SuppressWarnings("unchecked")
    private Map<String, Object> cast(Object obj) {
        return (Map<String, Object>) obj;
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

}

