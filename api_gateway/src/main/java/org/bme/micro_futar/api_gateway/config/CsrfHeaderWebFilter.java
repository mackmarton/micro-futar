package org.bme.micro_futar.api_gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * CSRF-védelem egyedi fejléccel: az állapotváltoztató /api/** kéréseknek hordozniuk kell a beállított fejlécet.
 * Idegen oldal űrlappal nem tud egyedi fejlécet küldeni, szkriptből pedig csak a CORS-ban engedélyezett origin.
 *
 * <p>Szándékosan nem {@code @Component}: a Spring Security láncban, a CORS után fut ({@link SecurityConfig}),
 * így a preflightot a CORS kezeli, és a 403-as válaszon is rajta vannak a CORS-fejlécek.
 */
public class CsrfHeaderWebFilter implements WebFilter {

    public static final String ERROR_CODE = "CSRF_HEADER_MISSING";

    private static final Logger LOG = LoggerFactory.getLogger(CsrfHeaderWebFilter.class);
    private static final Set<HttpMethod> PROTECTED_METHODS =
            Set.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE);
    private static final PathPattern API_PATHS = PathPatternParser.defaultInstance.parse("/api/**");
    private static final byte[] ERROR_BODY = ("{\"type\":\"about:blank\",\"title\":\"Forbidden\",\"status\":403,"
            + "\"code\":\"" + ERROR_CODE + "\"}").getBytes(StandardCharsets.UTF_8);

    private final String headerName;
    private final String headerValue;
    private final List<PathPattern> exemptPaths;

    public CsrfHeaderWebFilter(String headerName, String headerValue, List<String> exemptPaths) {
        if (headerName.isBlank() || headerValue.isBlank()) {
            throw new IllegalArgumentException("app.csrf.header-name and app.csrf.header-value must not be blank");
        }
        this.headerName = headerName;
        this.headerValue = headerValue;
        this.exemptPaths = exemptPaths.stream().map(PathPatternParser.defaultInstance::parse).toList();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (!requiresHeader(request) || headerValue.equals(request.getHeaders().getFirst(headerName))) {
            return chain.filter(exchange);
        }

        LOG.warn("{}: {} {}", ERROR_CODE, request.getMethod(), request.getPath().value());
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return response.writeWith(Mono.fromSupplier(() -> response.bufferFactory().wrap(ERROR_BODY)));
    }

    private boolean requiresHeader(ServerHttpRequest request) {
        if (!PROTECTED_METHODS.contains(request.getMethod())) {
            return false;
        }
        PathContainer path = request.getPath().pathWithinApplication();
        return API_PATHS.matches(path) && exemptPaths.stream().noneMatch(pattern -> pattern.matches(path));
    }
}
