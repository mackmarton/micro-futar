package org.bme.micro_futar.api_gateway.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RedirectUriSessionWebFilter implements WebFilter {

    public static final String REDIRECT_URL = "REDIRECT_URL";
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    private static final String LOGIN_PATH_PREFIX = "/oauth2/authorization/keycloak";
    private static final String LOGOUT_PATH_PREFIX = "/logout";
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final String ROOT_DOMAIN = "micro-futar.hu";
    private static final String LOCALHOST = "localhost";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestPath = exchange.getRequest().getPath().value();
        if (!requestPath.startsWith(LOGIN_PATH_PREFIX) && !requestPath.startsWith(LOGOUT_PATH_PREFIX)) {
            return chain.filter(exchange);
        }

        String redirectUri = exchange.getRequest().getQueryParams().getFirst(REDIRECT_URI_PARAM);
        if (redirectUri == null || redirectUri.isBlank()) {
            return chain.filter(exchange);
        }

        URI parsedRedirectUri;
        try {
            parsedRedirectUri = URI.create(redirectUri);
        } catch (IllegalArgumentException ex) {
            return rejectInvalidRedirectUri(exchange);
        }

        if (!isAllowedRedirectUri(parsedRedirectUri)) {
            return rejectInvalidRedirectUri(exchange);
        }

        return exchange.getSession()
                .flatMap(session -> {
                    session.getAttributes().put(REDIRECT_URL, redirectUri);
                    return chain.filter(exchange);
                });
    }

    private boolean isAllowedRedirectUri(URI redirectUri) {
        String scheme = redirectUri.getScheme();
        String host = redirectUri.getHost();
        if (scheme == null || host == null) {
            return false;
        }

        String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
        String normalizedHost = host.toLowerCase(Locale.ROOT);
        if (!ALLOWED_SCHEMES.contains(normalizedScheme)) {
            return false;
        }

        return normalizedHost.equals(LOCALHOST)
                || normalizedHost.equals(ROOT_DOMAIN)
                || normalizedHost.endsWith("." + ROOT_DOMAIN);
    }

    private Mono<Void> rejectInvalidRedirectUri(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        return exchange.getResponse().setComplete();
    }
}
