package com.kapitalbank.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.Map;

@EnableConfigurationProperties(KapitalbankProperties.class)
@ConfigurationProperties(prefix = "kapitalbank")
@Getter
@Setter
public class KapitalbankProperties {

    private String mode;
    private String username;
    private String password;
    private String currency = "AZN";
    private String language = "az";
    private int timeoutSeconds = 30;

    private Map<String, String> baseUrl;
    private Map<String, String> hppUrl;

    private String redirectUrl;
    private boolean saveCards;

    private Logging logging = new Logging();

    @Getter
    @Setter
    public static class Logging {
        private boolean enabled;
    }
}

