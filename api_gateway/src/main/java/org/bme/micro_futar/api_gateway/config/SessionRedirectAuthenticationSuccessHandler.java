package org.bme.micro_futar.api_gateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class SessionRedirectAuthenticationSuccessHandler
        implements ServerAuthenticationSuccessHandler, ServerLogoutSuccessHandler {

    public static final String REDIRECT_URL = "REDIRECT_URL";
    private static final String DEFAULT_REDIRECT_URL = "http://localhost:5173/";

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        return doRedirect(webFilterExchange);
    }

    @Override
    public Mono<Void> onLogoutSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        return doRedirect(webFilterExchange);
    }

    private Mono<Void> doRedirect(WebFilterExchange webFilterExchange) {
        return webFilterExchange.getExchange().getSession().flatMap(session -> {
            Object redirectUrlAttribute = session.getAttributes().remove(REDIRECT_URL);
            String redirectUrl = redirectUrlAttribute != null
                    ? redirectUrlAttribute.toString()
                    : DEFAULT_REDIRECT_URL;

            webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
            webFilterExchange.getExchange().getResponse().getHeaders().setLocation(URI.create(redirectUrl));
            return webFilterExchange.getExchange().getResponse().setComplete();
        });
    }
}
