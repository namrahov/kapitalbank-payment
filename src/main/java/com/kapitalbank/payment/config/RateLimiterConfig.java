package com.kapitalbank.payment.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimiterConfig {

    @Bean
    public Bucket activateAccountBucket() {
        Bandwidth limit = Bandwidth.classic(20, Refill.intervally(20, Duration.ofHours(1)));
        return Bucket4j.builder().addLimit(limit).build();
    }


}

