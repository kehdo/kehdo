package app.kehdo.feature.upload

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

/**
 * Upload feature graph. After a successful generation, [onConversationReady]
 * is invoked with the conversation id so the caller (RootNavGraph) can
 * navigate to the reply screen.
 */
fun NavGraphBuilder.uploadGraph(
    onConversationReady: (String) -> Unit
) {
    navigation(
        route = UploadRoute.GRAPH,
        startDestination = UploadRoute.UPLOAD
    ) {
        composable(UploadRoute.UPLOAD) {
            UploadScreenRoute(onConversationReady = onConversationReady)
        }
    }
}
