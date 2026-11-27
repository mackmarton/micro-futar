package org.bme.micro_futar.payments.config;

import com.stripe.Stripe;
import com.stripe.StripeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String secretKey;

    /** Overridden only where a mock stands in for Stripe (docker compose, e2e). */
    @Value("${stripe.api-base:" + Stripe.LIVE_API_BASE + "}")
    private String apiBase;

    @Bean
    public StripeClient stripeClient() {
        return StripeClient.builder()
                .setApiKey(secretKey)
                .setApiBase(apiBase)
                .build();
    }
}
