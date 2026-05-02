package app.kehdo.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kehdo.core.common.Outcome
import app.kehdo.domain.auth.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUp: SignUpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpUiState())
    val state: StateFlow<SignUpUiState> = _state.asStateFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.EmailChanged -> _state.update { it.copy(email = event.email) }
            is SignUpEvent.PasswordChanged -> _state.update { it.copy(password = event.password) }
            SignUpEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = signUp(current.email, current.password)) {
                is Outcome.Success -> _state.update {
                    it.copy(isLoading = false, isSignedIn = true)
                }
                is Outcome.Failure -> _state.update {
                    it.copy(isLoading = false, error = result.error.code)
                }
            }
        }
    }
}

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val error: String? = null
)

sealed interface SignUpEvent {
    data class EmailChanged(val email: String) : SignUpEvent
    data class PasswordChanged(val password: String) : SignUpEvent
    data object Submit : SignUpEvent
}
