package app.kehdo.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kehdo.core.ui.components.AuroraButton
import app.kehdo.core.ui.theme.AuroraColors

@Composable
fun HomeScreenRoute(
    onStartNewReply: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onStartNewReply = onStartNewReply,
        onSignOut = viewModel::onSignOut
    )
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onStartNewReply: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        ) {
            Text(
                text = "You're in.",
                style = MaterialTheme.typography.headlineMedium,
                color = AuroraColors.Text
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.user?.email ?: "—",
                style = MaterialTheme.typography.bodyMedium,
                color = AuroraColors.TextDim
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Plan: ${state.user?.plan?.name ?: "—"}",
                style = MaterialTheme.typography.bodySmall,
                color = AuroraColors.TextMute
            )
            Spacer(Modifier.height(48.dp))
            AuroraButton(
                text = "New reply",
                onClick = onStartNewReply,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            AuroraButton(
                text = "Sign out",
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
