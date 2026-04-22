package org.bme.micro_futar.api_gateway.controllers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class AuthController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/api/auth/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal OidcUser oidcUser,
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient) {
        if (oidcUser == null) {
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", oidcUser.getFullName());
        userInfo.put("email", oidcUser.getEmail());
        userInfo.put("preferred_username", oidcUser.getPreferredUsername());

        userInfo.put("roles", extractRoles(oidcUser, authorizedClient));

        return ResponseEntity.ok(userInfo);
    }

    private List<String> extractRoles(OidcUser oidcUser, OAuth2AuthorizedClient authorizedClient) {
        Set<String> roles = new LinkedHashSet<>(extractRolesFromClaims(oidcUser.getClaims()));

        if (roles.isEmpty() && authorizedClient != null && authorizedClient.getAccessToken() != null) {
            roles.addAll(extractRolesFromAccessToken(authorizedClient.getAccessToken().getTokenValue()));
        }

        return new ArrayList<>(roles);
    }

    private List<String> extractRolesFromAccessToken(String accessToken) {
        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) {
                return List.of();
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = objectMapper.readValue(payloadJson, new TypeReference<>() {});
            return extractRolesFromClaims(claims);
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private List<String> extractRolesFromClaims(Map<String, Object> claims) {
        Set<String> roles = new LinkedHashSet<>();

        addRolesFromContainer(roles, claims.get("realm_access"));

        Object resourceAccess = claims.get("resource_access");
        if (resourceAccess instanceof Map<?, ?> resourceAccessMap) {
            for (Object clientAccess : resourceAccessMap.values()) {
                addRolesFromContainer(roles, clientAccess);
            }
        }

        return new ArrayList<>(roles);
    }

    @SuppressWarnings("unchecked")
    private void addRolesFromContainer(Set<String> roles, Object container) {
        if (!(container instanceof Map<?, ?> map)) {
            return;
        }

        Object roleValues = ((Map<String, Object>) map).get("roles");
        if (roleValues instanceof Iterable<?> iterable) {
            for (Object role : iterable) {
                if (role != null) {
                    roles.add(role.toString());
                }
            }
        }
    }
}