package app.kehdo.backend.conversation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for {@link Reply}.
 *
 * <p>Replies inherit deletion from their parent conversation via the
 * {@code ON DELETE CASCADE} FK in V2 — there's no soft-delete column on
 * {@code replies}.</p>
 */
public interface ReplyRepository extends JpaRepository<Reply, UUID> {

    @Query("""
            SELECT r FROM Reply r
            WHERE r.conversationId = :conversationId
            ORDER BY r.rank ASC
            """)
    List<Reply> findByConversationOrderByRank(UUID conversationId);
}
