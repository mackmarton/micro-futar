package org.bme.micro_futar.orders.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", "Unauthorized");

        // Provide specific error messages for different authentication failures
        String message = determineErrorMessage(authException);
        errorResponse.put("message", message);
        errorResponse.put("path", request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private String determineErrorMessage(AuthenticationException authException) {
        if (authException instanceof InvalidBearerTokenException bearerException) {
            String exceptionMessage = bearerException.getMessage();

            // Check if it's a JWT validation error
            if (exceptionMessage != null) {
                if (exceptionMessage.contains("Jwt expired")) {
                    return "Access token has expired. Please obtain a new token.";
                } else if (exceptionMessage.contains("Invalid token")) {
                    return "Invalid access token provided.";
                } else if (exceptionMessage.contains("signature")) {
                    return "Token signature validation failed.";
                }
            }
            return "Invalid or malformed access token.";
        }

        if (authException.getMessage() != null && authException.getMessage().contains("expired")) {
            return "Access token has expired. Please obtain a new token.";
        }

        return "Authentication required. Please provide a valid access token.";
    }
}

