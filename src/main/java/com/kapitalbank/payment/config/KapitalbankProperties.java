package com.kapitalbank.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "kapitalbank")
public class KapitalbankProperties {

    /**
     * test | production
     */
    private String mode;

    private Api api = new Api();
    private Hpp hpp = new Hpp();
    private Redirect redirect = new Redirect();
    private Logging logging = new Logging();

    // =========================
    // API (server-to-server)
    // =========================
    @Getter
    @Setter
    public static class Api {
        private Map<String, String> baseUrl;
        private String merchantId;
        private String terminalId;
        private int timeoutSeconds = 30;
    }

    // =========================
    // HPP (Hosted Payment Page)
    // =========================
    @Getter
    @Setter
    public static class Hpp {
        private String currency = "AZN";
        private String language = "az";
        private boolean saveCards;
    }

    // =========================
    // Redirect URLs
    // =========================
    @Getter
    @Setter
    public static class Redirect {
        private String callback;
        private String returnUrl;
        private String success;
        private String error;
    }

    // =========================
    // Logging
    // =========================
    @Getter
    @Setter
    public static class Logging {
        private boolean enabled;
    }

    // =========================
    // Helper methods (VERY useful)
    // =========================
    public String apiBaseUrl() {
        return api.getBaseUrl().get(mode);
    }

}


