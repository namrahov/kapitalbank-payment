package com.kapitalbank.payment.service;

import com.kapitalbank.payment.config.KapitalbankProperties;
import com.kapitalbank.payment.model.dto.KapitalbankCallbackResult;
import com.kapitalbank.payment.model.dto.OrderResponse;
import com.kapitalbank.payment.model.exception.KapitalbankException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class KapitalbankService {

    private final KapitalbankProperties props;
    private final WebClient.Builder webClientBuilder;

    /* =======================
       URL helpers
       ======================= */

    private String apiBaseUrl() {
        return props.apiBaseUrl();
    }

    private String hppBaseUrl() {
        return props.hppUrl();
    }

    /* =======================
       Order creation
       ======================= */

    public OrderResponse createOrder(Map<String, Object> data) {
        return createOrderByType("Order_SMS", data);
    }

    public OrderResponse createPreAuthOrder(Map<String, Object> data) {
        return createOrderByType("Order_DMS", data);
    }

    public OrderResponse createRecurringOrder(Map<String, Object> data) {
        return createOrderByType("Order_REC", data);
    }

    public OrderResponse createCardToCardOrder(Map<String, Object> data) {
        return createOrderByType("OCT", data);
    }

    public OrderResponse createInstallmentOrder(Map<String, Object> data, int months) {
        data.put("description", "TAKSIT=" + months);
        return createOrderByType("Order_SMS", data);
    }

    protected OrderResponse createOrderByType(
            String typeRid,
            Map<String, Object> data) {

        Map<String, Object> order = new HashMap<>();

        order.put("typeRid", typeRid);
        order.put("amount", data.get("amount").toString());
        order.put("currency", props.getHpp().getCurrency());
        order.put("language", props.getHpp().getLanguage());
        order.put("description", data.getOrDefault("description", ""));
        order.put("hppRedirectUrl", props.getRedirect().getCallback());

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

    public boolean isSuccessful(String status) {
        return List.of("FullyPaid", "PartiallyPaid", "Approved")
                .contains(status);
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

        Map<String, Object> details =
                getOrderDetails(Long.valueOf(orderId), true);

        Map<String, Object> order = cast(details.get("order"));
        String actualStatus =
                order.getOrDefault("status", "Unknown").toString();

        Long storedTokenId = extractStoredTokenId(order);

        return new KapitalbankCallbackResult(
                Long.valueOf(orderId),
                callbackStatus,
                actualStatus,
                isSuccessful(actualStatus),
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
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
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
            Object first = list.get(0);
            if (first instanceof Map<?, ?> token) {
                Object id = token.get("id");
                if (id != null) {
                    return Long.valueOf(id.toString());
                }
            }
        }
        return null;
    }

    private void logIfEnabled(String type, Object data) {
        if (props.getLogging().isEnabled()) {
            log.info("Kapitalbank [{}] {}", type, data);
        }
    }
}

