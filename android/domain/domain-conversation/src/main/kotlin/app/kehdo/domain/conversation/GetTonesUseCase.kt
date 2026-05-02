package app.kehdo.domain.conversation

import app.kehdo.core.common.Outcome
import javax.inject.Inject

/**
 * Fetch the available tones, grouped by mode. The UI uses this to render
 * the mode picker → tone drilldown.
 */
class GetTonesUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    suspend operator fun invoke(): Outcome<List<Tone>> = repository.getTones()
}
