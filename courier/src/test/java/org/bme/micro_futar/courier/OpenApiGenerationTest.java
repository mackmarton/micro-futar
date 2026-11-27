package org.bme.micro_futar.courier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The UI generates its API clients from the file written here (target/openapi.json). */
@IntegrationTest
class OpenApiGenerationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiDocumentIsWrittenIntoTargetDirectory() throws Exception {
        String openApiJson = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                // the response names no charset, MockMvc would fall back to ISO-8859-1
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(openApiJson).contains("\"openapi\"");
        write(openApiJson);
    }

    private static void write(String openApiJson) throws IOException {
        Path outputPath = Path.of("target", "openapi.json");
        Files.createDirectories(outputPath.getParent());
        Files.writeString(outputPath, openApiJson, StandardCharsets.UTF_8);
    }
}
