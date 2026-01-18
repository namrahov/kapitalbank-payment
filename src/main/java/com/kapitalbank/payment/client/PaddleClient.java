package com.kapitalbank.payment.client;

import com.kapitalbank.payment.config.PaddleProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class PaddleClient {

    private final WebClient webClient;
    private final PaddleProperties props;

    public PaddleClient(WebClient.Builder builder, PaddleProperties props) {
        this.props = props;

        String baseUrl = props.getApi().getBaseUrl().get(props.getMode());
        this.webClient = builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApi().getApiKey())
                .defaultHeader("Paddle-Version", props.getApi().getVersion())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Map<String, Object> createTransaction(Map<String, Object> body) {
        return webClient.post()
                .uri("/transactions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }
}

