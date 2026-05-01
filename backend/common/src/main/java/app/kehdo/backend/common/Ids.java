package app.kehdo.backend.common;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

/**
 * Helper for generating UUIDv7 primary keys.
 *
 * <p>Per {@code backend/CLAUDE.md}: "UUID v7 for all primary keys
 * (time-ordered, avoids enumeration)". UUIDv7 keeps the index-locality
 * benefits of sequential IDs while remaining globally unique and
 * non-guessable by clients.</p>
 *
 * <p>All entity {@code @Id} columns should be populated via
 * {@link #newId()} on creation; never let the database side-effect-generate
 * IDs.</p>
 */
public final class Ids {

    private Ids() {}

    /** Generate a fresh time-ordered UUIDv7. */
    public static UUID newId() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
