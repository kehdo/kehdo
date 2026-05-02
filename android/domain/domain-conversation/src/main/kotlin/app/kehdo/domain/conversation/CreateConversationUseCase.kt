package app.kehdo.domain.conversation

import app.kehdo.core.common.Outcome
import javax.inject.Inject

/**
 * Upload a screenshot and create a new conversation. The screenshot URI
 * must point to a local file the app has read access to (typically from
 * the system photo picker).
 */
class CreateConversationUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    suspend operator fun invoke(screenshotUri: String): Outcome<Conversation> {
        require(screenshotUri.isNotBlank()) { "Screenshot URI cannot be blank" }
        return repository.createConversation(screenshotUri)
    }
}
