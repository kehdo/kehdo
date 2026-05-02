package app.kehdo.feature.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

fun NavGraphBuilder.onboardingGraph(
    onFinished: () -> Unit
) {
    navigation(
        route = OnboardingRoute.GRAPH,
        startDestination = OnboardingRoute.ONBOARDING
    ) {
        composable(OnboardingRoute.ONBOARDING) {
            OnboardingScreenRoute(onFinished = onFinished)
        }
    }
}
