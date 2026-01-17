package com.kapitalbank.payment.config;

import com.kapitalbank.payment.client.KapitalbankClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class KapitalbankAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KapitalbankClient kapitalbankClient(
            KapitalbankProperties properties) {

        return new KapitalbankClient(properties);
    }
}