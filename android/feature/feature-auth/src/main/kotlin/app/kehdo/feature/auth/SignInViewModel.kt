package app.kehdo.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kehdo.core.common.Outcome
import app.kehdo.domain.auth.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sign-in screen ViewModel.
 *
 * Demonstrates the MVI-lite pattern used across all feature modules:
 *   1. Immutable State (data class)
 *   2. Sealed Event interface
 *   3. ViewModel reduces events into new state
 *
 * The Composable observes state via collectAsStateWithLifecycle().
 */
@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signIn: SignInUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignInUiState())
    val state: StateFlow<SignInUiState> = _state.asStateFlow()

    fun onEvent(event: SignInEvent) {
        when (event) {
            is SignInEvent.EmailChanged -> _state.update { it.copy(email = event.email) }
            is SignInEvent.PasswordChanged -> _state.update { it.copy(password = event.password) }
            SignInEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = signIn(current.email, current.password)) {
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

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val error: String? = null
)

sealed interface SignInEvent {
    data class EmailChanged(val email: String) : SignInEvent
    data class PasswordChanged(val password: String) : SignInEvent
    data object Submit : SignInEvent
}
