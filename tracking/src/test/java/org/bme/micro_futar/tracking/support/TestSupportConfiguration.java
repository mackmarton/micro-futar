package org.bme.micro_futar.tracking.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.testcontainers.kafka.KafkaContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestSupportConfiguration {

    @Bean
    DatabaseCleaner databaseCleaner(JdbcTemplate jdbcTemplate) {
        return new DatabaseCleaner(jdbcTemplate, () -> { });
    }

    @Bean
    KafkaTestClient kafkaTestClient(KafkaContainer kafka, Environment environment, ObjectMapper objectMapper,
                                    KafkaListenerEndpointRegistry listeners) {
        return new KafkaTestClient(kafka.getBootstrapServers(), environment, objectMapper, listeners);
    }
}
