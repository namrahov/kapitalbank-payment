package com.kapitalbank.payment.service;

import com.kapitalbank.payment.config.KapitalbankProperties;
import com.kapitalbank.payment.model.dto.KapitalbankCallbackResult;
import com.kapitalbank.payment.model.dto.OrderResponse;
import com.kapitalbank.payment.model.exception.KapitalbankException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KapitalbankService {

    private final KapitalbankProperties props;
    private final WebClient.Builder webClientBuilder;

    private String baseUrl() {
        return props.getBaseUrl().get(props.getMode());
    }

    private String hppUrl() {
        return props.getHppUrl().get(props.getMode());
    }

    /* =======================
       Language / Currency
       ======================= */

    public KapitalbankService setLanguage(String lang) {
        props.setLanguage(lang);
        return this;
    }

    public KapitalbankService setCurrency(String currency) {
        props.setCurrency(currency);
        return this;
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

    protected OrderResponse createOrderByType(String typeRid, Map<String, Object> data) {

        Map<String, Object> order = new HashMap<>();
        order.put("typeRid", typeRid);
        order.put("amount", data.get("amount").toString());
        order.put("currency", data.getOrDefault("currency", props.getCurrency()));
        order.put("language", data.getOrDefault("language", props.getLanguage()));
        order.put("description", data.getOrDefault("description", ""));
        order.put("hppRedirectUrl", data.getOrDefault("redirect_url", props.getRedirectUrl()));

        if (data.containsKey("title")) {
            order.put("title", data.get("title"));
        }

        if (props.isSaveCards() || Boolean.TRUE.equals(data.get("save_card"))) {
            order.put("hppCofCapturePurposes", List.of("UnspecifiedMit", "Cit", "Recurring"));
            order.put("aut", Map.of("purpose", "AddCard"));

            if (data.containsKey("stored_id")) {
                order.put("srcToken", Map.of("storedId", data.get("stored_id")));
            }
        }

        Map<String, Object> payload = Map.of("order", order);
        Map<String, Object> response = request("POST", "/order", payload);

        Map<String, Object> r = (Map<String, Object>) response.get("order");

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
       Redirect / URLs
       ======================= */

    public String getPaymentUrl(Long orderId, String password) {
        return hppUrl() + "?id=" + orderId + "&password=" + password;
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
        Map<String, Object> order = (Map<String, Object>) details.get("order");
        return order != null ? order.getOrDefault("status", "Unknown").toString() : "Unknown";
    }

    /* =======================
       Transactions
       ======================= */

    public Map<String, Object> executeTransaction(Long orderId, Map<String, Object> data) {
        return request("POST", "/order/" + orderId + "/exec-tran",
                Map.of("tran", data));
    }

    public boolean isSuccessful(String status) {
        return List.of("FullyPaid", "PartiallyPaid", "Approved").contains(status);
    }

    /* =======================
       Callback verification
       ======================= */

    @SuppressWarnings("unchecked")
    public KapitalbankCallbackResult verifyCallback(
            Map<String, String> params) {

        String orderId = params.getOrDefault("ID", params.get("id"));
        String callbackStatus = params.getOrDefault("STATUS", params.get("status"));

        if (orderId == null) {
            throw new KapitalbankException("Order ID tapılmadı callback-də");
        }

        Map<String, Object> details = getOrderDetails(Long.valueOf(orderId), true);
        Map<String, Object> order = (Map<String, Object>) details.get("order");

        String actualStatus = order.getOrDefault("status", "Unknown").toString();

        Long storedTokenId = null;
        Object tokens = order.get("storedTokens");
        if (tokens instanceof List<?> list && !list.isEmpty()) {
            Object first = list.getFirst();
            if (first instanceof Map<?, ?> token) {
                Object id = token.get("id");
                if (id != null) {
                    storedTokenId = Long.valueOf(id.toString());
                }
            }
        }

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

        String url = baseUrl() + endpoint;

        logIfEnabled("request", Map.of("method", method, "url", url));

        WebClient client = webClientBuilder
                .baseUrl(url)
                .defaultHeaders(h -> h.setBasicAuth(
                        props.getUsername(),
                        props.getPassword()
                ))
                .build();

        try {
            WebClient.ResponseSpec spec =
                    method.equals("GET")
                            ? client.get().retrieve()
                            : client.post().bodyValue(body).retrieve();

            Map<String, Object> response =
                    spec.bodyToMono(Map.class).block();

            logIfEnabled("response", response);
            return response;

        } catch (Exception ex) {
            throw new KapitalbankException(
                    "Kapitalbank API ilə əlaqə xətası: " + ex.getMessage());
        }
    }

    protected void logIfEnabled(String type, Object data) {
        if (props.getLogging().isEnabled()) {
            log.info("Kapitalbank [{}] {}", type, data);
        }
    }
}
