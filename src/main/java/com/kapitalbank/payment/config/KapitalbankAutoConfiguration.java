package com.kapitalbank.payment.config;

import com.kapitalbank.payment.service.KapitalbankService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(KapitalbankProperties.class)
public class KapitalbankAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KapitalbankService kapitalbankService(
            KapitalbankProperties props,
            WebClient.Builder builder) {

        return new KapitalbankService(props, builder);
    }
}
