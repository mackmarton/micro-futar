package org.bme.micro_futar.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableTestContainers
class OpenApiGenerationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void shouldGenerateOpenApiJsonIntoTargetDirectory() throws IOException {
        String openApiJson = testRestTemplate.getForObject("/v3/api-docs", String.class);

        assertThat(openApiJson)
                .isNotBlank()
                .contains("\"openapi\"");

        Path outputPath = Path.of("target", "openapi.json");
        Files.createDirectories(outputPath.getParent());
        Files.writeString(
                outputPath,
                openApiJson,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}

