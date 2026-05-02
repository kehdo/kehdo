package app.kehdo.domain.conversation

import app.kehdo.core.common.Outcome
import javax.inject.Inject

/** Soft-delete one conversation (user-initiated forget). */
class DeleteConversationUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    suspend operator fun invoke(conversationId: String): Outcome<Unit> {
        require(conversationId.isNotBlank()) { "Conversation id cannot be blank" }
        return repository.deleteConversation(conversationId)
    }
}
