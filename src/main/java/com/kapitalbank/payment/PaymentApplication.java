package com.kapitalbank.payment;

import com.kapitalbank.payment.config.KapitalbankProperties;
import com.kapitalbank.payment.config.PaddleProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        KapitalbankProperties.class,
        PaddleProperties.class
})
public class PaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentApplication.class, args);
	}

}
