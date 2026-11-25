package org.bme.micro_futar.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
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
    private final String defaultRedirectUrl;

    private final RedirectUriPolicy redirectUriPolicy;

    public SessionRedirectAuthenticationSuccessHandler(
            @Value("${app.default-redirect-url}") String defaultRedirectUrl,
            RedirectUriPolicy redirectUriPolicy) {
        this.defaultRedirectUrl = defaultRedirectUrl;
        this.redirectUriPolicy = redirectUriPolicy;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        // A bejelentkezés több kérésen át tart, ezért a cím a munkamenetben várakozik.
        return webFilterExchange.getExchange().getSession()
                .flatMap(session -> redirect(webFilterExchange, session.getAttributes().remove(REDIRECT_URL)));
    }

    @Override
    public Mono<Void> onLogoutSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        // A kijelentkezés egyetlen kérés, a címet a RedirectUriSessionWebFilter a kérés attribútumába teszi.
        return redirect(webFilterExchange, webFilterExchange.getExchange().getAttribute(REDIRECT_URL));
    }

    private Mono<Void> redirect(WebFilterExchange webFilterExchange, Object redirectUrlAttribute) {
        // A munkamenet (Redis) túlélheti a lista szigorítását, ezért a tárolt címet újra ellenőrizzük.
        String redirectUrl = redirectUrlAttribute != null && redirectUriPolicy.isAllowed(redirectUrlAttribute.toString())
                ? redirectUrlAttribute.toString()
                : defaultRedirectUrl;

        webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
        webFilterExchange.getExchange().getResponse().getHeaders().setLocation(URI.create(redirectUrl));
        return webFilterExchange.getExchange().getResponse().setComplete();
    }
}
