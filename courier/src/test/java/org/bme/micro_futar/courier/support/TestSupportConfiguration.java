package org.bme.micro_futar.courier.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bme.micro_futar.courier.config.auth.JwtAuthConverter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.kafka.KafkaContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestSupportConfiguration {

    @Bean
    DatabaseCleaner databaseCleaner(JdbcTemplate jdbcTemplate) {
        return new DatabaseCleaner(jdbcTemplate, () -> { });
    }

    @Bean
    KafkaTestClient kafkaTestClient(KafkaContainer kafka, Environment environment, ObjectMapper objectMapper,
                                    KafkaListenerEndpointRegistry listeners, KafkaTemplate<String, String> template) {
        return new KafkaTestClient(kafka.getBootstrapServers(), environment, objectMapper, listeners, template);
    }

    @Bean
    CourierScenario courierScenario(KafkaTestClient kafka) {
        return new CourierScenario(kafka);
    }

    @Bean
    TestJwts testJwts(JwtAuthConverter jwtAuthConverter) {
        return new TestJwts(jwtAuthConverter);
    }
}
