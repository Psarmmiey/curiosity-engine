package com.curiosityengine.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.curiosityengine.app.feature.auth.AuthScreen
import com.curiosityengine.app.feature.auth.AuthViewModel
import com.curiosityengine.app.feature.auth.SplashScreen
import com.curiosityengine.app.feature.onboarding.OnboardingScreen
import com.curiosityengine.app.feature.onboarding.OnboardingViewModel

@Composable
fun CuriosityNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavRoute.Splash.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(NavRoute.Splash.route) {
            SplashScreen(
                onAuthResult = { isAuthenticated ->
                    if (isAuthenticated) {
                        navController.navigate(NavRoute.Home.route) {
                            popUpTo(NavRoute.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(NavRoute.Auth.route) {
                            popUpTo(NavRoute.Splash.route) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(NavRoute.Auth.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            // We track userId so onboarding can receive it
            var signedInUserId by rememberSaveable { mutableStateOf<String?>(null) }

            // Observe the auth state to capture the userId for onboarding routing
            val authState by authViewModel.authState.collectAsStateWithLifecycle()

            AuthScreen(
                viewModel = authViewModel,
                onSignedIn = {
                    val userId = (authState as? com.curiosityengine.app.feature.auth.AuthState.Authenticated)?.userId
                    signedInUserId = userId
                    if (userId != null) {
                        navController.navigate(NavRoute.Onboarding.routeWithArg(userId)) {
                            popUpTo(NavRoute.Auth.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(NavRoute.Home.route) {
                            popUpTo(NavRoute.Auth.route) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(
            route = NavRoute.Onboarding.ROUTE_WITH_ARG,
            arguments = listOf(navArgument(NavRoute.Onboarding.ARG_USER_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString(NavRoute.Onboarding.ARG_USER_ID) ?: ""
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = onboardingViewModel,
                userId = userId,
                onComplete = {
                    navController.navigate(NavRoute.Home.route) {
                        popUpTo(NavRoute.Onboarding.ROUTE_WITH_ARG) { inclusive = true }
                    }
                },
            )
        }

        composable(NavRoute.Home.route) {
            // Placeholder — HomeScreen implemented by Agent I
        }

        composable(NavRoute.Journal.route) {
            // Placeholder — JournalScreen implemented by Agent L
        }

        composable(NavRoute.Profile.route) {
            // Placeholder — ProfileScreen implemented by Agent L
        }

        composable(
            route = NavRoute.Lesson.ROUTE,
            arguments = listOf(navArgument(NavRoute.Lesson.ARG) { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "curiosityengine://lesson/{lessonId}" }
            ),
        ) { backStackEntry ->
            // LessonScreen(lessonId = backStackEntry.arguments?.getString(NavRoute.Lesson.ARG) ?: "")
            // Implemented by Agent F
        }

        composable(
            route = NavRoute.Quiz.ROUTE,
            arguments = listOf(navArgument(NavRoute.Quiz.ARG) { type = NavType.StringType }),
        ) { backStackEntry ->
            // QuizScreen(lessonId = backStackEntry.arguments?.getString(NavRoute.Quiz.ARG) ?: "")
            // Implemented by Agent G
        }

        composable(
            route = NavRoute.QuizResults.ROUTE,
            arguments = listOf(navArgument(NavRoute.QuizResults.ARG) { type = NavType.StringType }),
        ) { backStackEntry ->
            // QuizResultsScreen(resultId = backStackEntry.arguments?.getString(NavRoute.QuizResults.ARG) ?: "")
            // Implemented by Agent G
        }

        composable(
            route = NavRoute.WeeklyReview.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = "curiosityengine://quiz/weekly" }
            ),
        ) {
            // Placeholder — WeeklyReviewScreen implemented by Agent G
        }

        composable(
            route = NavRoute.DoomScroll.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = "curiosityengine://doom-scroll" }
            ),
        ) {
            // Placeholder — DoomScrollScreen implemented by Agent J
        }

        composable(NavRoute.Settings.route) {
            // Placeholder — SettingsScreen implemented by Agent L
        }
    }
}
