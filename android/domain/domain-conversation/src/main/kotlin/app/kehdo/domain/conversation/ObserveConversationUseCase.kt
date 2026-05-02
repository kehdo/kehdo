package app.kehdo.domain.conversation

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Reactive observer for a single conversation by id. */
class ObserveConversationUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    operator fun invoke(conversationId: String): Flow<Conversation?> =
        repository.observe(conversationId)
}
