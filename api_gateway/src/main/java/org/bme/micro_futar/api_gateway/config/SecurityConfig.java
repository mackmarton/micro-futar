package org.bme.micro_futar.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final List<String> allowedOrigins;

    private final CsrfHeaderWebFilter csrfHeaderWebFilter;

    public SecurityConfig(
            @Value("${app.cors.allowed-origins}") List<String> allowedOrigins,
            @Value("${app.csrf.header-name}") String csrfHeaderName,
            @Value("${app.csrf.header-value}") String csrfHeaderValue,
            @Value("${app.csrf.exempt-paths}") List<String> csrfExemptPaths) {
        this.allowedOrigins = allowedOrigins;
        this.csrfHeaderWebFilter = new CsrfHeaderWebFilter(csrfHeaderName, csrfHeaderValue, csrfExemptPaths);
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            SessionRedirectAuthenticationSuccessHandler sessionRedirectAuthenticationSuccessHandler,
            LocaleAwareOAuth2AuthorizationRequestResolver localeAwareOAuth2AuthorizationRequestResolver) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/orders/**", "/api/tracking/**", "/api/payments/**", "/api/auth/me")
                        .permitAll()
                        .pathMatchers("/actuator/health/**")
                        .permitAll()
                        .anyExchange()
                        .authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationRequestResolver(localeAwareOAuth2AuthorizationRequestResolver)
                        .authenticationSuccessHandler(sessionRedirectAuthenticationSuccessHandler)
                )
                .logout(logout -> logout
                        .requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/logout"))
                        .logoutSuccessHandler(sessionRedirectAuthenticationSuccessHandler)
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .addFilterAt(csrfHeaderWebFilter, SecurityWebFiltersOrder.CSRF)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> {
                            if (exchange.getRequest().getPath().value().startsWith("/api/")) {
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                return exchange.getResponse().setComplete();
                            }
                            return new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/keycloak")
                                    .commence(exchange, ex);
                        })
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
