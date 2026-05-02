package app.kehdo.backend.conversation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for {@link Conversation}.
 *
 * <p>Active-only finders filter out soft-deleted rows; raw
 * {@link #findById} from {@link JpaRepository} still returns soft-deleted
 * conversations and should only be used by the cleanup job.</p>
 */
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("""
            SELECT c FROM Conversation c
            WHERE c.id = :id
              AND c.userId = :userId
              AND c.deletedAt IS NULL
            """)
    Optional<Conversation> findActiveByIdAndUser(UUID id, UUID userId);

    @Query("""
            SELECT c FROM Conversation c
            WHERE c.userId = :userId
              AND c.deletedAt IS NULL
            ORDER BY c.createdAt DESC
            """)
    Page<Conversation> findActiveByUser(UUID userId, Pageable pageable);
}
