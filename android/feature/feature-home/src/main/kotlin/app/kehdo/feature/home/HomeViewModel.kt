package app.kehdo.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kehdo.domain.auth.ObserveCurrentUserUseCase
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
    private val signOut: SignOutUseCase
) : ViewModel() {

    val state: StateFlow<HomeUiState> = observeCurrentUser()
        .map { HomeUiState(user = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    fun onSignOut() {
        viewModelScope.launch { signOut() }
    }
}

data class HomeUiState(
    val user: User? = null
)
