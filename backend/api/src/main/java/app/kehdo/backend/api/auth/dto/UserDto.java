package app.kehdo.backend.api.auth.dto;

import app.kehdo.backend.user.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Public projection of {@link User}.
 *
 * <p>Never expose {@code passwordHash}, {@code deletedAt}, or any field
 * the OpenAPI {@code User} schema doesn't include.</p>
 */
public record UserDto(
        UUID id,
        String email,
        String displayName,
        String plan,
        Instant createdAt) {

    public static UserDto from(User u) {
        return new UserDto(
                u.getId(),
                u.getEmail(),
                u.getDisplayName(),
                u.getPlan().name(),
                u.getCreatedAt());
    }
}
