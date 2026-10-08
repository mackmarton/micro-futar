package org.bme.micro_futar.api_gateway.config;

import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LocaleAwareOAuth2AuthorizationRequestResolver implements ServerOAuth2AuthorizationRequestResolver {

    private static final String UI_LOCALES_PARAM = "ui_locales";

    private final DefaultServerOAuth2AuthorizationRequestResolver delegate;

    public LocaleAwareOAuth2AuthorizationRequestResolver(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        this.delegate = new DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange) {
        return delegate.resolve(exchange).map(authorizationRequest -> withUiLocales(exchange, authorizationRequest));
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange, String clientRegistrationId) {
        return delegate.resolve(exchange, clientRegistrationId)
                .map(authorizationRequest -> withUiLocales(exchange, authorizationRequest));
    }

    private OAuth2AuthorizationRequest withUiLocales(ServerWebExchange exchange, OAuth2AuthorizationRequest authorizationRequest) {
        String uiLocales = exchange.getRequest().getQueryParams().getFirst(UI_LOCALES_PARAM);
        if (uiLocales == null || uiLocales.isBlank()) {
            return authorizationRequest;
        }

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(params -> params.put(UI_LOCALES_PARAM, uiLocales))
                .build();
    }
}
