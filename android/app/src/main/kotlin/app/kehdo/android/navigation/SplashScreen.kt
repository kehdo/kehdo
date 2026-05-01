package app.kehdo.android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kehdo.core.ui.theme.AuroraColors

@Composable
fun SplashScreen(
    onSignedIn: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val decision by viewModel.decision.collectAsStateWithLifecycle()
    LaunchedEffect(decision) {
        when (decision) {
            SplashDecision.SignedIn -> onSignedIn()
            SplashDecision.SignedOut -> onSignedOut()
            SplashDecision.Loading -> Unit
        }
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AuroraColors.Purple)
    }
}
