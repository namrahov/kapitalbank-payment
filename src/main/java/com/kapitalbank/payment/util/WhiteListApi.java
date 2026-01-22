package com.kapitalbank.payment.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhiteListApi {

    public static final String[] AUTH_WHITELIST = {
            "/v1/users/login",
            "/v1/users",
            "/v1/users/active",
            "/v1/users/change-email",
            "/v1/users/forget-password",
            "/v1/users/change-password",
            "/v1/users/info/*",
            "/v1/datasets/info/*",
            "/v1/datasets/data-file/*",
            "/v1/competitions/data-file/*",
            "/v1/datasets/*/feedback",
            "/v1/blogs/info/*",
            "/v1/blogs/*",
            "/v1/categories/all",
            "/v1/blogs/tags/*",
            "/v1/contacts",
            "/v1/datasets/public/page",
            "/v1/blogs/public/page",
            "/v1/translations/faqs",
            "/v1/datasets/*/comment",
            "/v1/datasets/*/dataset-update",
            "/v1/files/dataset-update-image/*",
            "/v1/competitions/*/page",
            "/v1/competitions/admin/*/page",
            "/v1/competitions/*/info",
            "/v1/competitions/*/comment",
            "/v1/files/upload/profile-image",
            "/v1/files/dataset-files/*/zip",
            "/v1/files/competition-data-files/*/zip",
            "/v1/files/data/*",
            "/v1/files/dataset-file/*",
            "/v1/files/dataset-image/*",
            "/v1/files/blog-image/*",
            "/v1/files/competition-image/*",
            "/v1/files/category-image/*",
            "/v1/files/profile-image/*",
            "/v1/translations/static",
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
