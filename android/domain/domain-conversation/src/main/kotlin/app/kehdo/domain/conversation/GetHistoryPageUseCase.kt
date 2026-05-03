package app.kehdo.domain.conversation

import app.kehdo.core.common.Outcome
import javax.inject.Inject

/**
 * Fetch one page of conversation history. Pass [cursor] = null for the
 * first page, then forward [HistoryPage.nextCursor] to load more. Null
 * cursor on the response means we're at the end.
 */
class GetHistoryPageUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    suspend operator fun invoke(
        limit: Int = 20,
        cursor: String? = null
    ): Outcome<HistoryPage> = repository.getHistoryPage(limit, cursor)
}
