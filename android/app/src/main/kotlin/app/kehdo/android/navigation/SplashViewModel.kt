package app.kehdo.android.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kehdo.domain.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Decides where to send the user on cold start: home if a refresh
 * token is on disk and still valid, onboarding otherwise.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _decision = MutableStateFlow<SplashDecision>(SplashDecision.Loading)
    val decision = _decision.asStateFlow()

    init {
        viewModelScope.launch {
            val restored = authRepository.tryRestoreSession()
            _decision.value = if (restored) SplashDecision.SignedIn else SplashDecision.SignedOut
        }
    }
}

sealed interface SplashDecision {
    data object Loading : SplashDecision
    data object SignedIn : SplashDecision
    data object SignedOut : SplashDecision
}
