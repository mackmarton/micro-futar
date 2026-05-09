package org.bme.micro_futar.courier;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
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

        waitForDatabaseReady();

        System.setProperty("SPRING_PROFILE", "local");
        System.setProperty("SERVER_PORT", "8080");
        System.setProperty("DATASOURCE_URL", postgreSQL.getJdbcUrl());
        System.setProperty("DATASOURCE_USERNAME", postgreSQL.getUsername());
        System.setProperty("DATASOURCE_PASSWORD", postgreSQL.getPassword());
        System.setProperty("KAFKA_BOOTSTRAP_SERVERS", kafka.getBootstrapServers());
        System.setProperty("KEYCLOAK_URL", "url");
        System.setProperty("API_GATEWAY_URL", "http://localhost:8085");
    }

    private static void waitForDatabaseReady() {
        int maxRetries = 30;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try (Connection conn = DriverManager.getConnection(
                    postgreSQL.getJdbcUrl(),
                    postgreSQL.getUsername(),
                    postgreSQL.getPassword())) {
                // Connection successful, database is ready
                System.out.println("Database connection verified successfully");
                return;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("Database did not become ready in time", e);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for database", ie);
                }
            }
        }
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // can be empty
    }
}

