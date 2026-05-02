package app.kehdo.feature.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kehdo.core.common.Outcome
import app.kehdo.domain.conversation.CreateConversationUseCase
import app.kehdo.domain.conversation.GenerateRepliesUseCase
import app.kehdo.domain.conversation.GetTonesUseCase
import app.kehdo.domain.conversation.Mode
import app.kehdo.domain.conversation.Tone
import app.kehdo.domain.user.GetUsageUseCase
import app.kehdo.domain.user.UsageSnapshot
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
 * Upload screen ViewModel. Drives the screenshot → tone-pick → generate
 * flow. On success, emits a [UploadEffect.NavigateToReply] one-shot effect
 * with the conversation id so the caller (UploadGraph) can navigate.
 *
 * MVI-lite per android/CLAUDE.md.
 */
@HiltViewModel
class UploadViewModel @Inject constructor(
    private val getTones: GetTonesUseCase,
    private val createConversation: CreateConversationUseCase,
    private val generateReplies: GenerateRepliesUseCase,
    private val getUsage: GetUsageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UploadUiState())
    val state: StateFlow<UploadUiState> = _state.asStateFlow()

    private val _effects = Channel<UploadEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadTones()
        loadUsage()
    }

    fun onEvent(event: UploadEvent) {
        when (event) {
            is UploadEvent.ScreenshotPicked -> _state.update {
                it.copy(screenshotUri = event.uri, error = null)
            }
            is UploadEvent.ModeSelected -> _state.update {
                it.copy(selectedMode = event.mode, selectedToneCode = null)
            }
            is UploadEvent.ToneSelected -> _state.update {
                it.copy(selectedToneCode = event.toneCode)
            }
            UploadEvent.GenerateClicked -> generate()
            UploadEvent.RetryLoadTones -> loadTones()
            UploadEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadUsage() {
        viewModelScope.launch {
            // Quota lookup is a soft signal — failures never block the
            // primary flow. The footer just renders nothing until refresh.
            (getUsage() as? Outcome.Success)?.let { result ->
                _state.update { it.copy(usage = result.value) }
            }
        }
    }

    private fun loadTones() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingTones = true, error = null) }
            when (val result = getTones()) {
                is Outcome.Success -> _state.update {
                    it.copy(isLoadingTones = false, tones = result.value)
                }
                is Outcome.Failure -> _state.update {
                    it.copy(isLoadingTones = false, error = result.error.code)
                }
            }
        }
    }

    private fun generate() {
        val current = _state.value
        val uri = current.screenshotUri ?: return
        val toneCode = current.selectedToneCode ?: return

        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, error = null) }

            val createResult = createConversation(uri)
            if (createResult is Outcome.Failure) {
                _state.update { it.copy(isGenerating = false, error = createResult.error.code) }
                return@launch
            }
            val conversation = (createResult as Outcome.Success).value

            when (val genResult = generateReplies(conversation.id, toneCode)) {
                is Outcome.Success -> {
                    _state.update { it.copy(isGenerating = false) }
                    // Refresh quota so a fast back-nav reflects the consumed
                    // reply without an extra network hop on the next mount.
                    loadUsage()
                    _effects.trySend(UploadEffect.NavigateToReply(conversation.id))
                }
                is Outcome.Failure -> _state.update {
                    it.copy(isGenerating = false, error = genResult.error.code)
                }
            }
        }
    }
}

data class UploadUiState(
    val screenshotUri: String? = null,
    val isLoadingTones: Boolean = false,
    val tones: List<Tone> = emptyList(),
    val selectedMode: Mode? = null,
    val selectedToneCode: String? = null,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val usage: UsageSnapshot? = null
) {
    val canGenerate: Boolean
        get() = screenshotUri != null && selectedToneCode != null && !isGenerating

    val tonesForSelectedMode: List<Tone>
        get() = selectedMode?.let { mode -> tones.filter { it.mode == mode } } ?: emptyList()
}

sealed interface UploadEvent {
    data class ScreenshotPicked(val uri: String) : UploadEvent
    data class ModeSelected(val mode: Mode) : UploadEvent
    data class ToneSelected(val toneCode: String) : UploadEvent
    data object GenerateClicked : UploadEvent
    data object RetryLoadTones : UploadEvent
    data object ErrorDismissed : UploadEvent
}

sealed interface UploadEffect {
    data class NavigateToReply(val conversationId: String) : UploadEffect
}
