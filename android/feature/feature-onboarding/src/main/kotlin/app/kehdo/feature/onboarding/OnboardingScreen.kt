@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package app.kehdo.feature.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.kehdo.core.ui.components.AuroraButton
import app.kehdo.core.ui.theme.AuroraColors
import kotlinx.coroutines.launch

private data class Pane(val title: String, val body: String)

private val PANES = listOf(
    Pane(
        title = "Capture the moment.",
        body = "Screenshot any chat — WhatsApp, iMessage, Slack. We figure out who said what."
    ),
    Pane(
        title = "Pick a tone.",
        body = "Eighteen flavours, from playful to professional. Sound like you, not a bot."
    ),
    Pane(
        title = "Send with confidence.",
        body = "Three replies in under six seconds. Refine with your own prompt if you like."
    )
)

@Composable
fun OnboardingScreenRoute(
    onFinished: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { PANES.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onFinished) {
                Text("Skip", color = AuroraColors.TextDim)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            OnboardingPane(PANES[page])
        }

        PageIndicator(
            count = PANES.size,
            current = pagerState.currentPage
        )

        Spacer(Modifier.height(24.dp))

        val isLast = pagerState.currentPage == PANES.size - 1
        AuroraButton(
            text = if (isLast) "Get started" else "Next",
            onClick = {
                if (isLast) onFinished()
                else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        )
    }
}

@Composable
private fun OnboardingPane(pane: Pane) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = pane.title,
            style = MaterialTheme.typography.headlineMedium,
            color = AuroraColors.Text,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = pane.body,
            style = MaterialTheme.typography.bodyLarge,
            color = AuroraColors.TextDim,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 360.dp)
        )
    }
}

@Composable
private fun PageIndicator(count: Int, current: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(count) { index ->
            val color = if (index == current) AuroraColors.Purple else AuroraColors.LineStrong
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

