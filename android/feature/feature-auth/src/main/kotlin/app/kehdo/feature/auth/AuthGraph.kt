package app.kehdo.feature.auth

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

/**
 * Auth feature graph. Hosts sign-in and sign-up screens.
 *
 * [onAuthSuccess] is called after a successful sign-in or sign-up so the
 * caller (RootNavGraph) can pop the auth stack and navigate to home.
 */
fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onAuthSuccess: () -> Unit
) {
    navigation(
        route = AuthRoute.GRAPH,
        startDestination = AuthRoute.SIGN_IN
    ) {
        composable(AuthRoute.SIGN_IN) {
            SignInScreenRoute(
                onSignedIn = onAuthSuccess,
                onNavigateToSignUp = { navController.navigate(AuthRoute.SIGN_UP) }
            )
        }
        composable(AuthRoute.SIGN_UP) {
            SignUpScreenRoute(
                onSignedIn = onAuthSuccess,
                onNavigateToSignIn = { navController.popBackStack() }
            )
        }
    }
}
