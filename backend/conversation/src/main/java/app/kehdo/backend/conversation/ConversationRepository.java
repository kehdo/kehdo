package app.kehdo.backend.conversation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
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

    /**
     * First-page keyset query — no cursor, take the newest {@code limit}
     * rows. Used by {@code GET /conversations} when the client doesn't
     * pass {@code ?cursor=}. Sorted by {@code (createdAt, id)} desc to
     * guarantee stable ordering when two conversations share the same
     * millisecond timestamp.
     */
    @Query("""
            SELECT c FROM Conversation c
            WHERE c.userId = :userId
              AND c.deletedAt IS NULL
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Conversation> findActivePageByUser(UUID userId, Pageable pageable);

    /**
     * Subsequent-page keyset query — fetches rows strictly older than the
     * cursor position. Expanded form of the row-value comparison
     * {@code (createdAt, id) < (:cursorCreatedAt, :cursorId)} since JPQL
     * doesn't support multi-column comparisons portably.
     */
    @Query("""
            SELECT c FROM Conversation c
            WHERE c.userId = :userId
              AND c.deletedAt IS NULL
              AND (c.createdAt < :cursorCreatedAt
                   OR (c.createdAt = :cursorCreatedAt AND c.id < :cursorId))
            ORDER BY c.createdAt DESC, c.id DESC
            """)
    List<Conversation> findActivePageByUserAfterCursor(
            UUID userId,
            Instant cursorCreatedAt,
            UUID cursorId,
            Pageable pageable);
}
