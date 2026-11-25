package org.bme.micro_futar.api_gateway.redirect;

import org.bme.micro_futar.api_gateway.config.RedirectProperties;
import org.bme.micro_futar.api_gateway.config.RedirectUriPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedirectUriPolicyTests {

    private static final RedirectUriPolicy PROD = new RedirectUriPolicy(
            new RedirectProperties(List.of("https"), List.of(), List.of("micro-futar.hu")),
            "https://micro-futar.hu/");
    private static final RedirectUriPolicy LOCAL = new RedirectUriPolicy(
            new RedirectProperties(List.of("http", "https"), List.of("localhost"), List.of()),
            "http://localhost:5173/");

    private final ApplicationContextRunner context = new ApplicationContextRunner()
            .withUserConfiguration(RedirectUriPolicy.class)
            .withPropertyValues("app.default-redirect-url=https://micro-futar.hu/");

    @ParameterizedTest
    @ValueSource(strings = {"https://micro-futar.hu", "https://app.micro-futar.hu", "HTTPS://Courier.Micro-Futar.HU/#/x"})
    void prodAccepts(String uri) {
        assertThat(PROD.isAllowed(uri)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://micro-futar.hu", "http://localhost:5173", "https://localhost",
            "https://micro-futar.hu@evil.com", "https://evil.com@micro-futar.hu", "//evil.com", "//micro-futar.hu",
            "https://evil.com/?x=micro-futar.hu", "https://evil.com#@micro-futar.hu", "https://evil.com\\@micro-futar.hu",
            "https://micro-futar.hu.evil.com", "https://evilmicro-futar.hu", "https:evil.com", "/relative",
            "javascript:alert(1)", "javascript://micro-futar.hu/%0Aalert(1)", "data:text/html,x", "not a uri"})
    void prodRejects(String uri) {
        assertThat(PROD.isAllowed(uri)).isFalse();
    }

    @Test
    void localAcceptsOnlyLocalhost() {
        assertThat(LOCAL.isAllowed("http://localhost:5173")).isTrue();
        assertThat(LOCAL.isAllowed("https://micro-futar.hu")).isFalse();
        assertThat(LOCAL.isAllowed("http://admin.localhost")).isFalse();
    }

    @Test
    void valuesAreNormalized() {
        RedirectProperties properties =
                new RedirectProperties(List.of(" HTTPS ", ""), List.of(" LocalHost "), List.of(" Micro-Futar.HU "));

        assertThat(properties.allowedSchemes()).containsExactly("https");
        assertThat(properties.allowedHosts()).containsExactly("localhost");
        assertThat(properties.allowedDomainSuffixes()).containsExactly("micro-futar.hu");
    }

    @ParameterizedTest
    @CsvSource({
            "'', localhost, micro-futar.hu",
            "'  ', localhost, micro-futar.hu",
            "https, '', ''",
            "https, ' , ', ' '",
            "'', '', ''"
    })
    void startupFailsOnAnEmptyConfiguration(String schemes, String hosts, String suffixes) {
        context.withPropertyValues(
                        "app.redirect.allowed-schemes=" + schemes,
                        "app.redirect.allowed-hosts=" + hosts,
                        "app.redirect.allowed-domain-suffixes=" + suffixes)
                .run(started -> assertThat(started).hasFailed()
                        .getFailure().hasStackTraceContaining("must not"));
    }

    @Test
    void startupFailsWithoutAnyRedirectProperty() {
        context.run(started -> assertThat(started).hasFailed());
    }

    @Test
    void startupSucceedsWithSuffixesOnly() {
        context.withPropertyValues(
                        "app.redirect.allowed-schemes=https",
                        "app.redirect.allowed-domain-suffixes=micro-futar.hu")
                .run(started -> assertThat(started).hasNotFailed());
    }

    @Test
    void defaultRedirectUrlMustBeAllowed() {
        RedirectProperties properties = new RedirectProperties(List.of("https"), List.of(), List.of("micro-futar.hu"));

        assertThatThrownBy(() -> new RedirectUriPolicy(properties, "http://localhost:5173/"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("app.default-redirect-url");
    }
}
