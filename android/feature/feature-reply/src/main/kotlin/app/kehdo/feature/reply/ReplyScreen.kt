package app.kehdo.feature.reply

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kehdo.core.ui.components.AuroraButton
import app.kehdo.core.ui.theme.AuroraColors
import app.kehdo.domain.conversation.Conversation
import app.kehdo.domain.conversation.ConversationStatus
import app.kehdo.domain.conversation.Reply

@Composable
fun ReplyScreenRoute(
    onDone: () -> Unit,
    viewModel: ReplyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    ReplyScreen(
        state = state,
        onCopy = { reply ->
            copyToClipboard(context, reply.text)
            viewModel.onEvent(ReplyEvent.CopyClicked(reply.id))
        },
        onFavorite = { reply -> viewModel.onEvent(ReplyEvent.FavoriteToggled(reply.id)) },
        onDone = onDone
    )
}

@Composable
internal fun ReplyScreen(
    state: ReplyUiState,
    onCopy: (Reply) -> Unit,
    onFavorite: (Reply) -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Suggested replies",
            style = MaterialTheme.typography.headlineMedium,
            color = AuroraColors.Text
        )

        when (state.conversation?.status) {
            null, ConversationStatus.PENDING_UPLOAD, ConversationStatus.PROCESSING -> {
                Text(
                    text = "Working on it…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuroraColors.TextDim
                )
            }
            ConversationStatus.FAILED -> {
                Text(
                    text = "Couldn't generate replies (${state.conversation.failureReason ?: "UNKNOWN"}).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuroraColors.Pink
                )
            }
            ConversationStatus.READY -> {
                state.conversation.replies.forEach { reply ->
                    ReplyCard(
                        reply = reply,
                        onCopy = { onCopy(reply) },
                        onFavorite = { onFavorite(reply) }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        AuroraButton(
            text = "Done",
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ReplyCard(
    reply: Reply,
    onCopy: () -> Unit,
    onFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AuroraColors.Surface)
            .border(1.dp, AuroraColors.SurfaceElevated, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = reply.text,
            style = MaterialTheme.typography.bodyLarge,
            color = AuroraColors.Text
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionPill(
                label = if (reply.isCopied) "Copied" else "Copy",
                isAccented = reply.isCopied,
                onClick = onCopy,
                modifier = Modifier.weight(1f)
            )
            ActionPill(
                label = if (reply.isFavorited) "★ Saved" else "☆ Save",
                isAccented = reply.isFavorited,
                onClick = onFavorite,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionPill(
    label: String,
    isAccented: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (isAccented) AuroraColors.Purple else AuroraColors.SurfaceElevated
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = AuroraColors.Text
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("kehdo reply", text))
}
