package app.kehdo.android.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.kehdo.core.ui.theme.AuroraColors
import app.kehdo.feature.auth.AuthRoute
import app.kehdo.feature.auth.authGraph

@Composable
fun RootNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthRoute.GRAPH
    ) {
        authGraph(
            navController = navController,
            onAuthSuccess = {
                navController.navigate(NavRoutes.HOME) {
                    popUpTo(AuthRoute.GRAPH) { inclusive = true }
                }
            }
        )
        composable(NavRoutes.HOME) { HomePlaceholder() }
    }
}

@Composable
private fun HomePlaceholder() {
    // Real home screen lands in PR 4 (`feat/and/home-shell`).
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Signed in. Home screen coming next.", color = AuroraColors.Text)
    }
}

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
