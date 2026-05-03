package app.kehdo.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kehdo.core.common.Outcome
import app.kehdo.domain.auth.ObserveCurrentUserUseCase
import app.kehdo.domain.auth.RefreshCurrentUserUseCase
import app.kehdo.domain.auth.SignOutUseCase
import app.kehdo.domain.auth.User
import app.kehdo.domain.user.GetUsageUseCase
import app.kehdo.domain.user.UsageSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Profile screen ViewModel. Combines the observed [User] (email, plan,
 * display name) with a freshly-fetched [UsageSnapshot] (daily quota).
 * The User model carries quota fields but they are stale by design —
 * we always read /me/usage on Profile mount so the percentage shown
 * matches what /generate would actually decrement.
 *
 * Sign-out delegates to [SignOutUseCase]; the root nav graph picks up
 * the auth-state change and bounces the user back to the auth flow.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    private val refreshCurrentUser: RefreshCurrentUserUseCase,
    private val getUsage: GetUsageUseCase,
    private val signOut: SignOutUseCase
) : ViewModel() {

    private val _usage = MutableStateFlow<UsageSnapshot?>(null)

    val state: StateFlow<ProfileUiState> = combine(
        observeCurrentUser(),
        _usage
    ) { user, usage -> ProfileUiState(user = user, usage = usage) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState()
        )

    init {
        // Same pattern as Home: hit GET /me on mount so the cached User
        // from sign-in doesn't drift. On 401 the auth repo clears tokens
        // and the root nav graph bounces back to onboarding.
        viewModelScope.launch { refreshCurrentUser() }
        loadUsage()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.RefreshUsage -> loadUsage()
            ProfileEvent.SignOutClicked -> viewModelScope.launch { signOut() }
        }
    }

    private fun loadUsage() {
        viewModelScope.launch {
            // Best-effort — a flaky network shouldn't blank the screen.
            // If the call fails the existing snapshot stays visible.
            (getUsage() as? Outcome.Success)?.let { _usage.value = it.value }
        }
    }
}

data class ProfileUiState(
    val user: User? = null,
    val usage: UsageSnapshot? = null
)

sealed interface ProfileEvent {
    data object RefreshUsage : ProfileEvent
    data object SignOutClicked : ProfileEvent
}
