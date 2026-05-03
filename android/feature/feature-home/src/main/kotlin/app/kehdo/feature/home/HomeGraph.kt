package app.kehdo.feature.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

fun NavGraphBuilder.homeGraph(
    onStartNewReply: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    navigation(
        route = HomeRoute.GRAPH,
        startDestination = HomeRoute.HOME
    ) {
        composable(HomeRoute.HOME) {
            HomeScreenRoute(
                onStartNewReply = onStartNewReply,
                onOpenHistory = onOpenHistory,
                onOpenProfile = onOpenProfile
            )
        }
    }
}
