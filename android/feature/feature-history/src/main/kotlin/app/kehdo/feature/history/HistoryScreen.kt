package app.kehdo.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import app.kehdo.domain.conversation.Conversation
import app.kehdo.domain.conversation.ConversationStatus
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreenRoute(
    onOpenConversation: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HistoryScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onOpenConversation = onOpenConversation,
        onBack = onBack
    )
}

@Composable
internal fun HistoryScreen(
    state: HistoryUiState,
    onEvent: (HistoryEvent) -> Unit,
    onOpenConversation: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineMedium,
            color = AuroraColors.Text
        )

        when {
            state.isRefreshing && state.items.isEmpty() -> CenteredSpinner()
            state.isEmpty -> EmptyState()
            state.items.isNotEmpty() -> HistoryList(
                state = state,
                onEvent = onEvent,
                onOpenConversation = onOpenConversation,
                modifier = Modifier.weight(1f)
            )
            state.error != null -> ErrorState(error = state.error, onRetry = { onEvent(HistoryEvent.RefreshClicked) })
        }

        AuroraButton(
            text = "Back",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HistoryList(
    state: HistoryUiState,
    onEvent: (HistoryEvent) -> Unit,
    onOpenConversation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.items, key = { it.id }) { conversation ->
            ConversationRow(
                conversation = conversation,
                onClick = {
                    if (conversation.status == ConversationStatus.READY) {
                        onOpenConversation(conversation.id)
                    }
                },
                onDelete = { onEvent(HistoryEvent.DeleteClicked(conversation.id)) }
            )
        }
        if (state.canLoadMore) {
            item {
                AuroraButton(
                    text = "Load more",
                    onClick = { onEvent(HistoryEvent.LoadMoreClicked) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (state.isLoadingMore) {
            item { CenteredSpinner(small = true) }
        }
        if (state.error != null) {
            item {
                Text(
                    text = "Couldn't refresh history (${state.error}). Tap retry.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuroraColors.Pink
                )
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: Conversation,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AuroraColors.Surface)
            .border(1.dp, AuroraColors.SurfaceElevated, RoundedCornerShape(16.dp))
            .clickable(enabled = conversation.status == ConversationStatus.READY, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.toneCode?.let { humanizeTone(it) } ?: statusLabel(conversation.status),
                style = MaterialTheme.typography.titleSmall,
                color = AuroraColors.Text
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatTimestamp(conversation.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = AuroraColors.TextMute
            )
            if (conversation.status != ConversationStatus.READY) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = statusLabel(conversation.status),
                    style = MaterialTheme.typography.labelSmall,
                    color = AuroraColors.TextDim
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AuroraColors.SurfaceElevated)
                .clickable(onClick = onDelete)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Delete",
                style = MaterialTheme.typography.labelSmall,
                color = AuroraColors.Pink
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No conversations yet. Tap + on Home to start your first reply.",
            style = MaterialTheme.typography.bodyMedium,
            color = AuroraColors.TextDim
        )
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Couldn't load history ($error)",
            style = MaterialTheme.typography.bodyMedium,
            color = AuroraColors.Pink
        )
        Spacer(Modifier.height(8.dp))
        AuroraButton(
            text = "Retry",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CenteredSpinner(small: Boolean = false) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(if (small) 20.dp else 32.dp),
            color = AuroraColors.Purple,
            strokeWidth = 2.dp
        )
    }
}

private fun statusLabel(status: ConversationStatus): String = when (status) {
    ConversationStatus.PENDING_UPLOAD -> "Uploading…"
    ConversationStatus.PROCESSING -> "Working on it…"
    ConversationStatus.READY -> "Ready"
    ConversationStatus.FAILED -> "Failed"
}

private fun humanizeTone(code: String): String =
    code.lowercase().replaceFirstChar { it.titlecase() }

private fun formatTimestamp(epochMs: Long): String {
    val fmt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    return fmt.format(Date(epochMs))
}
