package app.kehdo.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kehdo.domain.auth.ObserveCurrentUserUseCase
import app.kehdo.domain.auth.RefreshCurrentUserUseCase
import app.kehdo.domain.auth.SignOutUseCase
import app.kehdo.domain.auth.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    private val refreshCurrentUser: RefreshCurrentUserUseCase,
    private val signOut: SignOutUseCase
) : ViewModel() {

    val state: StateFlow<HomeUiState> = observeCurrentUser()
        .map { HomeUiState(user = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    init {
        // Hits GET /me so the home screen shows backend-of-record values
        // rather than whatever the AuthResponse cached at sign-in. On 401
        // the repository clears session state, which cascades to the root
        // nav graph and bounces the user back to auth.
        viewModelScope.launch { refreshCurrentUser() }
    }

    fun onSignOut() {
        viewModelScope.launch { signOut() }
    }
}

data class HomeUiState(
    val user: User? = null
)
