package app.kehdo.feature.profile

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

/**
 * Profile feature graph. [onBack] returns to the previous destination
 * (typically Home). Sign-out is handled inside the ViewModel; the root
 * nav graph picks up the auth-state change and bounces the user back
 * to the auth flow automatically — see RootNavGraph.LaunchedEffect.
 */
fun NavGraphBuilder.profileGraph(
    onBack: () -> Unit
) {
    navigation(
        route = ProfileRoute.GRAPH,
        startDestination = ProfileRoute.PROFILE
    ) {
        composable(ProfileRoute.PROFILE) {
            ProfileScreenRoute(onBack = onBack)
        }
    }
}
