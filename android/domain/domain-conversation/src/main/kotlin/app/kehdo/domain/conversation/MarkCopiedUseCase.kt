package app.kehdo.domain.conversation

import app.kehdo.core.common.Outcome
import javax.inject.Inject

/**
 * Mark a reply as copied. This is a behavioral signal feeding the voice
 * fingerprint (per ADR 0006) — copy = strong positive choice signal.
 */
class MarkCopiedUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    suspend operator fun invoke(replyId: String): Outcome<Unit> =
        repository.markCopied(replyId)
}
