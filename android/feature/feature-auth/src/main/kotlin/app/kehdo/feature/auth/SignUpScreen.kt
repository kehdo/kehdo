package app.kehdo.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kehdo.core.ui.components.AuroraButton
import app.kehdo.core.ui.theme.AuroraColors

@Composable
fun SignUpScreenRoute(
    onSignedIn: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) onSignedIn()
    }
    SignUpScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateToSignIn = onNavigateToSignIn
    )
}

@Composable
internal fun SignUpScreen(
    state: SignUpUiState,
    onEvent: (SignUpEvent) -> Unit,
    onNavigateToSignIn: () -> Unit
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
                text = "Create your account.",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                color = AuroraColors.Text
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Five free replies a day, forever. No card needed.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = AuroraColors.TextDim
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = { onEvent(SignUpEvent.EmailChanged(it)) },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = { onEvent(SignUpEvent.PasswordChanged(it)) },
                label = { Text("Password (8+ characters)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            AuroraButton(
                text = "Create account",
                onClick = { onEvent(SignUpEvent.Submit) },
                enabled = !state.isLoading && state.email.isNotBlank() && state.password.length >= 8,
                modifier = Modifier.fillMaxWidth()
            )

            if (state.isLoading) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = AuroraColors.Purple
                )
            }
            if (state.error != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = state.error,
                    color = AuroraColors.Pink,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(24.dp))
            TextButton(
                onClick = onNavigateToSignIn,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Already have an account? Sign in", color = AuroraColors.PurpleBright)
            }
        }
    }
}
