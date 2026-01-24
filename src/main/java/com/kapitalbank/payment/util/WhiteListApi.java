package com.kapitalbank.payment.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhiteListApi {

    public static final String[] AUTH_WHITELIST = {
            "/v1/users/login",
            "/v1/users",
            "/v1/license",
            "/v1/users/active",
            "/v1/users/change-email",
            "/v1/users/forget-password",
            "/v1/users/change-password",
            "/v1/users/info/*",
            "/api/payments/callback",
            "/payment/kapitalbank/**",
            "/actuator/health",
            "/internal/licence/check-trial",
            "/v1/contacts",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    public static final Map<String, List<String>> JWT_FILTER_WHITELIST;
   // private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    static {
        JWT_FILTER_WHITELIST = new HashMap<>();
        for (String uri : AUTH_WHITELIST) {
            JWT_FILTER_WHITELIST.put(uri, Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        }
        JWT_FILTER_WHITELIST.put("/v1/competitions/*", Arrays.asList("GET"));
        JWT_FILTER_WHITELIST.put("/v1/datasets/*", Arrays.asList("GET"));
    }

}
