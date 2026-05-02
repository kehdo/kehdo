package app.kehdo.domain.conversation

import app.kehdo.core.common.Outcome
import javax.inject.Inject

/**
 * Trigger reply generation for an already-uploaded conversation. Returns
 * the conversation with 3 replies attached when status flips to READY.
 */
class GenerateRepliesUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    suspend operator fun invoke(
        conversationId: String,
        toneCode: String
    ): Outcome<Conversation> {
        require(conversationId.isNotBlank()) { "Conversation id cannot be blank" }
        require(toneCode.isNotBlank()) { "Tone code cannot be blank" }
        return repository.generateReplies(conversationId, toneCode)
    }
}
