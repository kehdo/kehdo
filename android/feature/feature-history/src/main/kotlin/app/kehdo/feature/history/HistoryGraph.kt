package app.kehdo.feature.history

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

/**
 * History feature graph. Tapping a row that's READY navigates to the
 * Reply screen (the same one used right after generation) so users can
 * re-copy or re-favorite. PENDING_UPLOAD/PROCESSING/FAILED rows render
 * status-only and aren't tappable.
 */
fun NavGraphBuilder.historyGraph(
    onOpenConversation: (String) -> Unit,
    onBack: () -> Unit
) {
    navigation(
        route = HistoryRoute.GRAPH,
        startDestination = HistoryRoute.HISTORY
    ) {
        composable(HistoryRoute.HISTORY) {
            HistoryScreenRoute(
                onOpenConversation = onOpenConversation,
                onBack = onBack
            )
        }
    }
}
