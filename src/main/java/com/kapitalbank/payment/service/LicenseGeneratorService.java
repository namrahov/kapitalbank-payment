package com.kapitalbank.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.kapitalbank.payment.model.dto.LicensePayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Base64;
import java.util.Objects;

@Service
public class LicenseGeneratorService {

    private final String hmacSecret;

    public LicenseGeneratorService(
            @Value("${license.hmac-secret}") String hmacSecret
    ) {
        this.hmacSecret = Objects.requireNonNull(
                hmacSecret, "LICENSE_HMAC_SECRET must be set");
    }

    public String generate(LicensePayload payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            String json = objectMapper.writeValueAsString(payload);
            String payloadB64 = base64UrlEncode(json.getBytes(StandardCharsets.UTF_8));
            String sigB64 = base64UrlEncode(hmacSha256(payloadB64));
            return payloadB64 + "." + sigB64;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate license key", e);
        }
    }

    private byte[] hmacSha256(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}

