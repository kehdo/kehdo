package app.kehdo.domain.conversation

import app.kehdo.core.common.Outcome
import javax.inject.Inject

/** Toggle a reply's favorited state. */
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    suspend operator fun invoke(replyId: String): Outcome<Unit> =
        repository.toggleFavorite(replyId)
}
