package org.bme.micro_futar.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Locale;

/** Eldönti, hogy egy visszairányítási cím engedélyezett-e. */
@Component
@EnableConfigurationProperties(RedirectProperties.class)
public class RedirectUriPolicy {

    private final RedirectProperties properties;

    public RedirectUriPolicy(RedirectProperties properties,
                             @Value("${app.default-redirect-url}") String defaultRedirectUrl) {
        this.properties = properties;
        if (!isAllowed(defaultRedirectUrl)) {
            throw new IllegalArgumentException(
                    "app.default-redirect-url is not allowed by app.redirect.*: " + defaultRedirectUrl);
        }
    }

    public boolean isAllowed(String redirectUri) {
        URI uri;
        try {
            uri = URI.create(redirectUri);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        String scheme = uri.getScheme();
        String host = uri.getHost();
        // Userinfo-ra nincs szükség, viszont megtévesztő (https://micro-futar.hu@evil.com).
        if (scheme == null || host == null || uri.getRawUserInfo() != null) {
            return false;
        }

        if (!properties.allowedSchemes().contains(scheme.toLowerCase(Locale.ROOT))) {
            return false;
        }

        String normalizedHost = host.toLowerCase(Locale.ROOT);
        return properties.allowedHosts().contains(normalizedHost)
                || properties.allowedDomainSuffixes().stream()
                .anyMatch(domain -> normalizedHost.equals(domain) || normalizedHost.endsWith("." + domain));
    }
}
