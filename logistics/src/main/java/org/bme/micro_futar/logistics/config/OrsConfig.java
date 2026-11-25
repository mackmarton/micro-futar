package org.bme.micro_futar.logistics.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/** The OpenRouteService calls (matrix and geocoding) must never hang a request thread, hence the timeouts. */
@Configuration
public class OrsConfig {

    @Bean
    public RestTemplate orsRestTemplate(
            @Value("${open-route-service.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${open-route-service.read-timeout-ms:5000}") long readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return new RestTemplate(requestFactory);
    }
}
