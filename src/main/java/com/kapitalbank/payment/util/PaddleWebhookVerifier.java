package com.kapitalbank.payment.util;

import com.kapitalbank.payment.config.PaddleProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

@Component
public class PaddleWebhookVerifier {

    private final PaddleProperties props;

    public PaddleWebhookVerifier(PaddleProperties props) {
        this.props = props;
    }

    public boolean verify(String paddleSignatureHeader, byte[] rawBody) {
        if (paddleSignatureHeader == null || paddleSignatureHeader.isBlank()) return false;

        Map<String, String> parts = parseSignatureHeader(paddleSignatureHeader);
        String ts = parts.get("ts");
        String h1 = parts.get("h1");
        if (ts == null || h1 == null) return false;

        String signedPayload = ts + ":" + new String(rawBody, StandardCharsets.UTF_8);

        String expected = hmacSha256Hex(props.getWebhook().getEndpointSecret(), signedPayload);
        return constantTimeEquals(expected, h1);
    }

    private Map<String, String> parseSignatureHeader(String header) {
        Map<String, String> out = new HashMap<>();
        for (String seg : header.split(";")) {
            String[] kv = seg.trim().split("=", 2);
            if (kv.length == 2) out.put(kv[0].trim(), kv[1].trim());
        }
        return out;
    }

    private String hmacSha256Hex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Webhook HMAC failure", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(x, y);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte bb : bytes) sb.append(String.format("%02x", bb));
        return sb.toString();
    }
}

