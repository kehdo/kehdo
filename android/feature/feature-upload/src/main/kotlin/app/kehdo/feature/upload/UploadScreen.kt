package app.kehdo.feature.upload

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import app.kehdo.core.ui.components.AuroraButton
import app.kehdo.core.ui.theme.AuroraColors
import app.kehdo.domain.conversation.Mode
import app.kehdo.domain.conversation.Tone
import app.kehdo.domain.user.UsageSnapshot

@Composable
fun UploadScreenRoute(
    onConversationReady: (String) -> Unit,
    viewModel: UploadViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is UploadEffect.NavigateToReply -> onConversationReady(effect.conversationId)
            }
        }
    }

    UploadScreen(state = state, onEvent = viewModel::onEvent)
}

@Composable
internal fun UploadScreen(
    state: UploadUiState,
    onEvent: (UploadEvent) -> Unit
) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onEvent(UploadEvent.ScreenshotPicked(uri.toString()))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "New reply",
            style = MaterialTheme.typography.headlineMedium,
            color = AuroraColors.Text
        )
        Text(
            text = "Pick a screenshot of the conversation, choose a tone, and we'll suggest replies.",
            style = MaterialTheme.typography.bodyMedium,
            color = AuroraColors.TextDim
        )

        ScreenshotPicker(
            screenshotUri = state.screenshotUri,
            onPick = {
                photoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )

        ModeSelector(
            selectedMode = state.selectedMode,
            onSelect = { onEvent(UploadEvent.ModeSelected(it)) }
        )

        if (state.selectedMode != null) {
            ToneSelector(
                tones = state.tonesForSelectedMode,
                selectedToneCode = state.selectedToneCode,
                isLoading = state.isLoadingTones,
                onSelect = { onEvent(UploadEvent.ToneSelected(it.code)) }
            )
        }

        Spacer(Modifier.height(8.dp))

        if (state.error != null) {
            Text(
                text = "Couldn't generate replies (${state.error}). Tap retry.",
                style = MaterialTheme.typography.bodySmall,
                color = AuroraColors.Pink
            )
        }

        AuroraButton(
            text = if (state.isGenerating) "Generating…" else "Generate replies",
            onClick = { onEvent(UploadEvent.GenerateClicked) },
            enabled = state.canGenerate,
            modifier = Modifier.fillMaxWidth()
        )

        if (state.usage != null) {
            QuotaFooter(usage = state.usage)
        }
    }
}

@Composable
private fun QuotaFooter(usage: UsageSnapshot) {
    val text = when {
        usage.isUnlimited -> "Unlimited replies on your plan."
        usage.remaining == 0 -> "Daily limit reached. Resets at midnight UTC."
        else -> "${usage.remaining} of ${usage.dailyLimit} replies left today."
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = if (usage.remaining == 0 && !usage.isUnlimited) AuroraColors.Pink
                else AuroraColors.TextDim,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ScreenshotPicker(
    screenshotUri: String?,
    onPick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.66f)
            .clip(RoundedCornerShape(20.dp))
            .background(AuroraColors.Surface)
            .border(1.dp, AuroraColors.SurfaceElevated, RoundedCornerShape(20.dp))
            .clickable(onClick = onPick),
        contentAlignment = Alignment.Center
    ) {
        if (screenshotUri == null) {
            Text(
                text = "Tap to pick a screenshot",
                style = MaterialTheme.typography.bodyMedium,
                color = AuroraColors.TextDim
            )
        } else {
            AsyncImage(
                model = screenshotUri,
                contentDescription = "Selected screenshot",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ModeSelector(
    selectedMode: Mode?,
    onSelect: (Mode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Mode",
            style = MaterialTheme.typography.labelLarge,
            color = AuroraColors.Text
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Mode.entries.forEach { mode ->
                ModeChip(
                    mode = mode,
                    isSelected = mode == selectedMode,
                    onClick = { onSelect(mode) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ModeChip(
    mode: Mode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (isSelected) AuroraColors.Purple else AuroraColors.Surface
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .border(
                width = 1.dp,
                color = if (isSelected) AuroraColors.Purple else AuroraColors.SurfaceElevated,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mode.name.lowercase().replaceFirstChar { it.titlecase() },
            style = MaterialTheme.typography.labelMedium,
            color = AuroraColors.Text
        )
    }
}

@Composable
private fun ToneSelector(
    tones: List<Tone>,
    selectedToneCode: String?,
    isLoading: Boolean,
    onSelect: (Tone) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Tone",
            style = MaterialTheme.typography.labelLarge,
            color = AuroraColors.Text
        )
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxWidth().height(44.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AuroraColors.Purple,
                    strokeWidth = 2.dp
                )
            }
            tones.isEmpty() -> Text(
                text = "No tones in this mode.",
                style = MaterialTheme.typography.bodySmall,
                color = AuroraColors.TextMute
            )
            else -> LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tones, key = { it.code }) { tone ->
                    ToneChip(
                        tone = tone,
                        isSelected = tone.code == selectedToneCode,
                        onClick = { onSelect(tone) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToneChip(
    tone: Tone,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val background = if (isSelected) AuroraColors.Purple else AuroraColors.Surface
    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .border(
                width = 1.dp,
                color = if (isSelected) AuroraColors.Purple else AuroraColors.SurfaceElevated,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${tone.emoji ?: ""} ${tone.name}".trim(),
            style = MaterialTheme.typography.labelMedium,
            color = AuroraColors.Text
        )
    }
}
