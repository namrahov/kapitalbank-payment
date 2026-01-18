package com.kapitalbank.payment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kapitalbank.payment.util.PaddleWebhookVerifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/paddle")
public class PaddleWebhookController {

    private final PaddleWebhookVerifier verifier;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaddleWebhookController(PaddleWebhookVerifier verifier) {
        this.verifier = verifier;
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> handle(
            @RequestHeader(value = "Paddle-Signature", required = false) String signature,
            @RequestBody byte[] rawBody
    ) throws Exception {

        if (!verifier.verify(signature, rawBody)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid signature");
        }

        // rawBody verify olduqdan sonra JSON parse edin
        JsonNode root = objectMapper.readTree(rawBody);

        String eventType = root.path("event_type").asText(null);
        JsonNode data = root.path("data");
        JsonNode customData = data.path("custom_data");

        String email = customData.path("email").asText(null);
        String instanceId = customData.path("instanceId").asText(null);

        // IMPORTANT: idempotency
        // event id-ni payload-dan götürüb DB-də unique saxlayın,
        // təkrar webhook gələndə ignore edin. (Paddle də bunu best practice kimi vurğulayır.) :contentReference[oaicite:11]{index=11}

        if ("transaction.completed".equals(eventType)) {
            // 1) payment tamamlandı → licence generate → email göndər
        }

        if ("subscription.created".equals(eventType)) {
            // subscription yaradıldı (çox vaxt completed transaction-dan sonra) :contentReference[oaicite:12]{index=12}
        }

        return ResponseEntity.ok("ok");
    }
}

