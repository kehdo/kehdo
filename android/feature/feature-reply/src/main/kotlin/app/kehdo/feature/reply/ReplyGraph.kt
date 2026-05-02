package app.kehdo.feature.reply

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument

/**
 * Reply feature graph. Reads the conversation id from the route arg and
 * renders the suggested replies. [onDone] returns control to the caller.
 */
fun NavGraphBuilder.replyGraph(
    onDone: () -> Unit
) {
    navigation(
        route = ReplyRoute.GRAPH,
        startDestination = ReplyRoute.REPLY_WITH_ARG
    ) {
        composable(
            route = ReplyRoute.REPLY_WITH_ARG,
            arguments = listOf(
                navArgument(ReplyRoute.ARG_CONVERSATION_ID) { type = NavType.StringType }
            )
        ) {
            ReplyScreenRoute(onDone = onDone)
        }
    }
}
