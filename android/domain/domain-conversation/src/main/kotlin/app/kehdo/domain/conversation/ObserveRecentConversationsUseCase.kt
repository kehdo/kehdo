package app.kehdo.domain.conversation

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Reactive list of recent conversations for the History screen. */
class ObserveRecentConversationsUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    operator fun invoke(limit: Int = 50): Flow<List<Conversation>> =
        repository.observeRecent(limit)
}
