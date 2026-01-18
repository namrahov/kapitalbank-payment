package com.kapitalbank.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "paddle")
@Getter
@Setter
public class PaddleProperties {
    private String mode;
    private Api api = new Api();
    private Webhook webhook = new Webhook();

    @Getter
    @Setter
    public static class Api {
        private Map<String, String> baseUrl = new HashMap<>();
        private String apiKey;
        private String version;
    }

    @Getter
    @Setter
    public static class Webhook {
        private String endpointSecret;
    }

}

