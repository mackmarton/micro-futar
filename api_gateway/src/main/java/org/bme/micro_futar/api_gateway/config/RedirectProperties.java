package org.bme.micro_futar.api_gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A bejelentkezés és kijelentkezés utáni visszairányítás engedélyezett címei. Hibás vagy hiányos beállítással a
 * gateway nem indul el.
 *
 * @param allowedSchemes        engedélyezett sémák
 * @param allowedHosts          pontosan egyező hostnevek
 * @param allowedDomainSuffixes domainek, amelyek maguk és az aldomainjeik is engedélyezettek
 */
@ConfigurationProperties("app.redirect")
public record RedirectProperties(
        List<String> allowedSchemes,
        List<String> allowedHosts,
        List<String> allowedDomainSuffixes) {

    public RedirectProperties {
        allowedSchemes = normalize(allowedSchemes);
        allowedHosts = normalize(allowedHosts);
        allowedDomainSuffixes = normalize(allowedDomainSuffixes);

        if (allowedSchemes.isEmpty()) {
            throw new IllegalArgumentException("app.redirect.allowed-schemes must not be empty");
        }
        if (allowedHosts.isEmpty() && allowedDomainSuffixes.isEmpty()) {
            throw new IllegalArgumentException(
                    "app.redirect.allowed-hosts and app.redirect.allowed-domain-suffixes must not both be empty");
        }
    }

    private static List<String> normalize(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(value -> value.strip().toLowerCase(Locale.ROOT))
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
