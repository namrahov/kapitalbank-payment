package com.kapitalbank.payment.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapitalbank.payment.model.dto.LicensePayload;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class LicenseEncoder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LicenseEncoder() {}

    public static String encode(LicensePayload payload) {
        try {
            String json = MAPPER.writeValueAsString(payload);
            return Base64.getEncoder()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("License encoding failed", e);
        }
    }

    public static LicensePayload decode(String encoded) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encoded);
            return MAPPER.readValue(decoded, LicensePayload.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid license format", e);
        }
    }
}
