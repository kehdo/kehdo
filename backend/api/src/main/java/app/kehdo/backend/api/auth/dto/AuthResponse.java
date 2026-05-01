package app.kehdo.backend.api.auth.dto;

import app.kehdo.backend.auth.service.AuthService.AuthResult;

import java.time.Instant;

/**
 * Wire shape for the four auth endpoints, matching the OpenAPI
 * {@code AuthResponse} schema in {@code contracts/openapi/kehdo.v1.yaml}.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserDto user) {

    public static AuthResponse from(AuthResult result, Instant now) {
        long expiresIn = Math.max(0, result.accessTokenExpiresAt().getEpochSecond() - now.getEpochSecond());
        return new AuthResponse(
                result.accessToken(),
                result.refreshToken(),
                expiresIn,
                UserDto.from(result.user()));
    }
}
