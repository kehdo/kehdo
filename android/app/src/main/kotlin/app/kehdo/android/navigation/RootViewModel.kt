package app.kehdo.android.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kehdo.domain.auth.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase
) : ViewModel() {

    /**
     * Tri-state: null until the first user emission lands, then true/false.
     * RootNavGraph waits for null → false to bounce sign-outs back to auth
     * without flashing the home screen during the cold-start splash.
     */
    val isSignedIn: StateFlow<Boolean?> = observeCurrentUser()
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}
