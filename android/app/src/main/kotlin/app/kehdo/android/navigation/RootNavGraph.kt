package app.kehdo.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.kehdo.feature.auth.AuthRoute
import app.kehdo.feature.auth.authGraph
import app.kehdo.feature.home.HomeRoute
import app.kehdo.feature.home.homeGraph
import app.kehdo.feature.onboarding.OnboardingRoute
import app.kehdo.feature.onboarding.onboardingGraph
import app.kehdo.feature.reply.ReplyRoute
import app.kehdo.feature.reply.replyGraph
import app.kehdo.feature.upload.UploadRoute
import app.kehdo.feature.upload.uploadGraph

@Composable
fun RootNavGraph(
    rootViewModel: RootViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val isSignedIn by rootViewModel.isSignedIn.collectAsStateWithLifecycle()

    // Watch for sign-out happening anywhere in the app and bounce back to auth.
    LaunchedEffect(isSignedIn) {
        if (isSignedIn == false) {
            val current = navController.currentDestination?.parent?.route
            val onAuthOrSplash = current == AuthRoute.GRAPH ||
                current == OnboardingRoute.GRAPH ||
                navController.currentDestination?.route == NavRoutes.SPLASH
            if (!onAuthOrSplash) {
                navController.navigate(AuthRoute.GRAPH) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onSignedIn = {
                    navController.navigate(HomeRoute.GRAPH) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                onSignedOut = {
                    navController.navigate(OnboardingRoute.GRAPH) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        onboardingGraph(
            onFinished = {
                navController.navigate(AuthRoute.GRAPH) {
                    popUpTo(OnboardingRoute.GRAPH) { inclusive = true }
                }
            }
        )
        authGraph(
            navController = navController,
            onAuthSuccess = {
                navController.navigate(HomeRoute.GRAPH) {
                    popUpTo(AuthRoute.GRAPH) { inclusive = true }
                }
            }
        )
        homeGraph(
            onStartNewReply = {
                navController.navigate(UploadRoute.GRAPH)
            }
        )
        uploadGraph(
            onConversationReady = { conversationId ->
                navController.navigate(ReplyRoute.reply(conversationId)) {
                    popUpTo(UploadRoute.GRAPH) { inclusive = true }
                }
            }
        )
        replyGraph(
            onDone = {
                navController.navigate(HomeRoute.GRAPH) {
                    popUpTo(HomeRoute.GRAPH) { inclusive = true }
                }
            }
        )
    }
}

object NavRoutes {
    const val SPLASH = "splash"
}
