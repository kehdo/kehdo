package app.kehdo.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kehdo.core.common.Outcome
import app.kehdo.domain.conversation.Conversation
import app.kehdo.domain.conversation.DeleteConversationUseCase
import app.kehdo.domain.conversation.GetHistoryPageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * History screen ViewModel — paginated list of past conversations.
 *
 * Pagination is driven by the backend's opaque cursor (see
 * `GET /v1/conversations`). [HistoryUiState.nextCursor] mirrors what the
 * server returned; null = end of stream and we hide the "load more"
 * affordance.
 *
 * Delete is optimistic: we drop the row from local state immediately and
 * roll back if the network call fails. Same pattern Gmail uses on swipe-
 * to-archive — the immediate visual response is more important than
 * holding the row "until the server confirms."
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryPage: GetHistoryPageUseCase,
    private val deleteConversation: DeleteConversationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    private val _effects = Channel<HistoryEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadFirstPage()
    }

    fun onEvent(event: HistoryEvent) {
        when (event) {
            HistoryEvent.RefreshClicked -> loadFirstPage()
            HistoryEvent.LoadMoreClicked -> loadMore()
            is HistoryEvent.DeleteClicked -> delete(event.conversationId)
            HistoryEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadFirstPage() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val result = getHistoryPage(limit = PAGE_SIZE, cursor = null)) {
                is Outcome.Success -> _state.update {
                    it.copy(
                        isRefreshing = false,
                        items = result.value.items,
                        nextCursor = result.value.nextCursor
                    )
                }
                is Outcome.Failure -> _state.update {
                    it.copy(isRefreshing = false, error = result.error.code)
                }
            }
        }
    }

    private fun loadMore() {
        val current = _state.value
        val cursor = current.nextCursor ?: return
        if (current.isLoadingMore) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true, error = null) }
            when (val result = getHistoryPage(limit = PAGE_SIZE, cursor = cursor)) {
                is Outcome.Success -> _state.update {
                    it.copy(
                        isLoadingMore = false,
                        items = it.items + result.value.items,
                        nextCursor = result.value.nextCursor
                    )
                }
                is Outcome.Failure -> _state.update {
                    it.copy(isLoadingMore = false, error = result.error.code)
                }
            }
        }
    }

    private fun delete(conversationId: String) {
        val before = _state.value.items
        // Optimistic remove — UI feels instant. Roll back on failure.
        _state.update { it.copy(items = it.items.filter { c -> c.id != conversationId }) }
        viewModelScope.launch {
            when (val result = deleteConversation(conversationId)) {
                is Outcome.Success -> _effects.trySend(HistoryEffect.Deleted(conversationId))
                is Outcome.Failure -> _state.update {
                    it.copy(items = before, error = result.error.code)
                }
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}

data class HistoryUiState(
    val items: List<Conversation> = emptyList(),
    val nextCursor: String? = null,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null
) {
    val canLoadMore: Boolean get() = nextCursor != null && !isLoadingMore && !isRefreshing
    val isEmpty: Boolean get() = items.isEmpty() && !isRefreshing && error == null
}

sealed interface HistoryEvent {
    data object RefreshClicked : HistoryEvent
    data object LoadMoreClicked : HistoryEvent
    data class DeleteClicked(val conversationId: String) : HistoryEvent
    data object ErrorDismissed : HistoryEvent
}

sealed interface HistoryEffect {
    data class Deleted(val conversationId: String) : HistoryEffect
}
