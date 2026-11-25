package org.bme.micro_futar.api_gateway.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RedirectUriSessionWebFilter implements WebFilter {

    public static final String REDIRECT_URL = "REDIRECT_URL";
    private static final String REDIRECT_URI_PARAM = "redirect_uri";
    private static final String LOGIN_PATH_PREFIX = "/oauth2/authorization/keycloak";
    private static final String LOGOUT_PATH = "/logout";

    private final RedirectUriPolicy redirectUriPolicy;

    public RedirectUriSessionWebFilter(RedirectUriPolicy redirectUriPolicy) {
        this.redirectUriPolicy = redirectUriPolicy;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestPath = exchange.getRequest().getPath().value();
        boolean isLogout = LOGOUT_PATH.equals(requestPath) && HttpMethod.POST.equals(exchange.getRequest().getMethod());
        if (!isLogout && !requestPath.startsWith(LOGIN_PATH_PREFIX)) {
            return chain.filter(exchange);
        }

        String redirectUri = exchange.getRequest().getQueryParams().getFirst(REDIRECT_URI_PARAM);
        if (redirectUri == null || redirectUri.isBlank()) {
            return chain.filter(exchange);
        }

        if (!redirectUriPolicy.isAllowed(redirectUri)) {
            return rejectInvalidRedirectUri(exchange);
        }

        // Kijelentkezésnél nem nyúlunk a munkamenethez: egy idegen oldalról, süti nélkül érkező POST új munkamenetet
        // nyitna, és a válasz sütije felülírná a felhasználó élő munkamenetét.
        if (isLogout) {
            exchange.getAttributes().put(REDIRECT_URL, redirectUri);
            return chain.filter(exchange);
        }

        return exchange.getSession()
                .flatMap(session -> {
                    session.getAttributes().put(REDIRECT_URL, redirectUri);
                    return chain.filter(exchange);
                });
    }

    private Mono<Void> rejectInvalidRedirectUri(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        return exchange.getResponse().setComplete();
    }
}
