package app.kehdo.feature.reply

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kehdo.domain.conversation.Conversation
import app.kehdo.domain.conversation.MarkCopiedUseCase
import app.kehdo.domain.conversation.ObserveConversationUseCase
import app.kehdo.domain.conversation.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Reply screen ViewModel. Observes the conversation by id (passed via
 * navigation arg) and reactively renders the 3 reply suggestions. Copy
 * and favorite actions emit behavioral signals (marked locally for now;
 * will sync to backend once Phase-4 interaction-signal endpoint lands).
 */
@HiltViewModel
class ReplyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeConversation: ObserveConversationUseCase,
    private val markCopied: MarkCopiedUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase
) : ViewModel() {

    private val conversationId: String = checkNotNull(
        savedStateHandle[ReplyRoute.ARG_CONVERSATION_ID]
    ) { "Missing conversationId nav arg" }

    val state: StateFlow<ReplyUiState> = observeConversation(conversationId)
        .map { conv -> ReplyUiState(conversation = conv) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReplyUiState()
        )

    fun onEvent(event: ReplyEvent) {
        when (event) {
            is ReplyEvent.CopyClicked -> viewModelScope.launch { markCopied(event.replyId) }
            is ReplyEvent.FavoriteToggled -> viewModelScope.launch { toggleFavorite(event.replyId) }
        }
    }
}

data class ReplyUiState(
    val conversation: Conversation? = null
)

sealed interface ReplyEvent {
    data class CopyClicked(val replyId: String) : ReplyEvent
    data class FavoriteToggled(val replyId: String) : ReplyEvent
}
