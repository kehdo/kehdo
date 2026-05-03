package app.kehdo.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kehdo.core.ui.components.AuroraButton
import app.kehdo.core.ui.theme.AuroraColors
import app.kehdo.domain.auth.User
import app.kehdo.domain.user.UsageSnapshot

@Composable
fun ProfileScreenRoute(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack
    )
}

@Composable
internal fun ProfileScreen(
    state: ProfileUiState,
    onEvent: (ProfileEvent) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = AuroraColors.Text
        )

        IdentityCard(user = state.user)

        UsageCard(usage = state.usage)

        Spacer(Modifier.weight(1f))

        AuroraButton(
            text = "Sign out",
            onClick = { onEvent(ProfileEvent.SignOutClicked) },
            modifier = Modifier.fillMaxWidth()
        )
        AuroraButton(
            text = "Back",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )

        // Account-deletion is a backlog item (GDPR Article 17 — needs a
        // confirm + cooldown flow); show it as visible-but-deferred so
        // users know it's planned without us shipping a half-baked path.
        Text(
            text = "Delete my account — coming soon",
            style = MaterialTheme.typography.bodySmall,
            color = AuroraColors.TextMute
        )
    }
}

@Composable
private fun IdentityCard(user: User?) {
    SurfaceCard {
        if (user == null) {
            Text(
                text = "Loading…",
                style = MaterialTheme.typography.bodyMedium,
                color = AuroraColors.TextDim
            )
        } else {
            Text(
                text = user.displayName ?: user.email,
                style = MaterialTheme.typography.titleMedium,
                color = AuroraColors.Text
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = AuroraColors.TextDim
            )
            Spacer(Modifier.height(12.dp))
            PlanBadge(plan = user.plan)
        }
    }
}

@Composable
private fun PlanBadge(plan: User.Plan) {
    val label = when (plan) {
        User.Plan.FREE -> "Starter (free)"
        User.Plan.PRO -> "Pro"
        User.Plan.UNLIMITED -> "Unlimited"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AuroraColors.SurfaceElevated)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = AuroraColors.Purple
        )
    }
}

@Composable
private fun UsageCard(usage: UsageSnapshot?) {
    SurfaceCard {
        Text(
            text = "Daily reply quota",
            style = MaterialTheme.typography.titleSmall,
            color = AuroraColors.Text
        )
        Spacer(Modifier.height(8.dp))
        when {
            usage == null -> Text(
                text = "Loading…",
                style = MaterialTheme.typography.bodyMedium,
                color = AuroraColors.TextDim
            )
            usage.isUnlimited -> Text(
                text = "Unlimited replies on your plan.",
                style = MaterialTheme.typography.bodyMedium,
                color = AuroraColors.TextDim
            )
            else -> {
                val progress = if (usage.dailyLimit > 0) {
                    usage.dailyUsed.toFloat() / usage.dailyLimit.toFloat()
                } else 0f
                Text(
                    text = "${usage.remaining} of ${usage.dailyLimit} replies left today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (usage.remaining == 0) AuroraColors.Pink else AuroraColors.TextDim
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = AuroraColors.Purple,
                    trackColor = AuroraColors.SurfaceElevated
                )
            }
        }
    }
}

@Composable
private fun SurfaceCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AuroraColors.Surface)
            .border(1.dp, AuroraColors.SurfaceElevated, RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column { content() }
    }
}
