package org.bme.micro_futar.orders;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

class TestcontainersConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final String KAFKA_IMAGE = "apache/kafka-native:latest";
    private static final String POSTGRES_IMAGE = "postgres:latest";

    private static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE));
    private static final PostgreSQLContainer<?> postgreSQL = new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE))
            .waitingFor(Wait.forListeningPort())
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2))
            .withStartupTimeout(Duration.ofMinutes(2));

    static {
        Startables.deepStart(kafka, postgreSQL).join();

        System.setProperty("SPRING_PROFILE", "local");
        System.setProperty("SERVER_PORT", "8080");
        System.setProperty("DATASOURCE_URL", postgreSQL.getJdbcUrl());
        System.setProperty("DATASOURCE_USERNAME", postgreSQL.getUsername());
        System.setProperty("DATASOURCE_PASSWORD", postgreSQL.getPassword());
        System.setProperty("KAFKA_BOOTSTRAP_SERVERS", kafka.getBootstrapServers());
        System.setProperty("KEYCLOAK_URL", "url");
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // can be empty
    }
}
