package app.kehdo.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Top-level navigation graph for the app.
 * Each feature module exposes its own `NavGraphBuilder` extension.
 *
 * As features are implemented, wire them up here:
 *
 *   onboardingGraph(navController)
 *   authGraph(navController)
 *   homeGraph(navController)
 *   ...
 */
@Composable
fun RootNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(NavRoutes.SPLASH) {
            // TODO: replace with feature-onboarding splash composable
            // For now, an empty scaffold that lets the app launch successfully.
        }
    }
}

/** Top-level route constants. Feature modules contribute their own. */
object NavRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val AUTH = "auth"
    const val HOME = "home"
    const val UPLOAD = "upload"
    const val REPLY = "reply/{conversationId}"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val PAYWALL = "paywall"
}
