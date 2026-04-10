package com.curiosityengine.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

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
            // Placeholder — SplashScreen implemented by Agent E
            // SplashScreen(navController = navController)
        }

        composable(NavRoute.Auth.route) {
            // Placeholder — AuthScreen implemented by Agent E
        }

        composable(NavRoute.Onboarding.route) {
            // Placeholder — OnboardingScreen implemented by Agent E
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
