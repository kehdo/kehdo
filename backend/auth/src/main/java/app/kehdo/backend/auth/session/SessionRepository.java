package app.kehdo.backend.auth.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

    @Modifying
    @Query("""
            UPDATE Session s
            SET s.revokedAt = :now
            WHERE s.userId = :userId
              AND s.revokedAt IS NULL
            """)
    int revokeAllActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
}
