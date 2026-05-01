package app.kehdo.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for {@link User}.
 *
 * <p>Active-only finders ({@link #findActiveByEmail}, {@link #findActiveById})
 * filter out soft-deleted rows; raw {@link #findById} from {@link JpaRepository}
 * returns soft-deleted users too — only use it from the cleanup job.</p>
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.email) = LOWER(:email)
              AND u.deletedAt IS NULL
            """)
    Optional<User> findActiveByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE u.id = :id
              AND u.deletedAt IS NULL
            """)
    Optional<User> findActiveById(UUID id);

    @Query("""
            SELECT COUNT(u) > 0 FROM User u
            WHERE LOWER(u.email) = LOWER(:email)
              AND u.deletedAt IS NULL
            """)
    boolean existsActiveByEmail(String email);
}
