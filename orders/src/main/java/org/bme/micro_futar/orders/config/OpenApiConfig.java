package org.bme.micro_futar.orders.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.servers.Server;
import org.jspecify.annotations.NonNull;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

//This class is needed to have correct routing for use with api gateway.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer applyGatewayPrefix() {
        return openApi -> {
            Paths newPaths = replacePath(openApi);
            Server gatewayServer = setUpGatewayServer();
            openApi.servers(List.of(gatewayServer));
            openApi.setPaths(newPaths);
        };
    }

    private static @NonNull Server setUpGatewayServer() {
        Server gatewayServer = new Server();
        gatewayServer.setUrl("http://localhost:8085");
        gatewayServer.setDescription("API Gateway");
        return gatewayServer;
    }

    private static @NonNull Paths replacePath(OpenAPI openApi) {
        Paths newPaths = new Paths();
        openApi.getPaths().forEach((path, item) -> {
            String newPath = path.replaceFirst("^/api/", "/api/orders/");
            newPaths.addPathItem(newPath, item);
        });
        return newPaths;
    }
}