package com.curiosityengine.app.ui.navigation

import androidx.compose.runtime.Composable
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
import com.curiosityengine.app.feature.doomscroll.DoomScrollScreen
import com.curiosityengine.app.feature.home.HomeScreen
import com.curiosityengine.app.feature.journal.JournalScreen
import com.curiosityengine.app.feature.lesson.LessonScreen
import com.curiosityengine.app.feature.onboarding.OnboardingScreen
import com.curiosityengine.app.feature.onboarding.OnboardingViewModel
import com.curiosityengine.app.feature.profile.ProfileScreen
import com.curiosityengine.app.feature.quiz.QuizResultsScreen
import com.curiosityengine.app.feature.quiz.QuizScreen
import com.curiosityengine.app.feature.quiz.WeeklyReviewScreen
import com.curiosityengine.app.feature.settings.SettingsScreen

@Composable
fun CuriosityNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavRoute.Splash.route,
    onStartRealtimeSync: (userId: String) -> Unit = {},
    onStopRealtimeSync: () -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {

        // ── Splash ────────────────────────────────────────────────────────────
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

        // ── Auth ──────────────────────────────────────────────────────────────
        composable(NavRoute.Auth.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            AuthScreen(
                viewModel = authViewModel,
                onSignedIn = { userId ->
                    onStartRealtimeSync(userId)
                    // Check if onboarding is needed; route to Onboarding with userId
                    navController.navigate(NavRoute.Onboarding.create(userId)) {
                        popUpTo(NavRoute.Auth.route) { inclusive = true }
                    }
                },
            )
        }

        // ── Onboarding ────────────────────────────────────────────────────────
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

        // ── Home ──────────────────────────────────────────────────────────────
        composable(NavRoute.Home.route) {
            HomeScreen(
                onNavigateToLesson = { lessonId ->
                    navController.navigate(NavRoute.Lesson.create(lessonId))
                },
                onNavigateToDoomScroll = {
                    navController.navigate(NavRoute.DoomScroll.route)
                },
                onNavigateToWeeklyReview = {
                    navController.navigate(NavRoute.WeeklyReview.route)
                },
            )
        }

        // ── Journal ───────────────────────────────────────────────────────────
        composable(NavRoute.Journal.route) {
            JournalScreen(
                onNavigateToLesson = { lessonId ->
                    navController.navigate(NavRoute.Lesson.create(lessonId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        // ── Profile ───────────────────────────────────────────────────────────
        composable(NavRoute.Profile.route) {
            ProfileScreen(
                onSignOut = {
                    onStopRealtimeSync()
                    navController.navigate(NavRoute.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        // ── Lesson ────────────────────────────────────────────────────────────
        composable(
            route = NavRoute.Lesson.ROUTE,
            arguments = listOf(navArgument(NavRoute.Lesson.ARG) { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "curiosityengine://lesson/{lessonId}" }
            ),
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString(NavRoute.Lesson.ARG) ?: ""
            LessonScreen(
                lessonId = lessonId,
                onNavigateToQuiz = { id ->
                    navController.navigate(NavRoute.Quiz.create(id))
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        // ── Quiz ──────────────────────────────────────────────────────────────
        composable(
            route = NavRoute.Quiz.ROUTE,
            arguments = listOf(navArgument(NavRoute.Quiz.ARG) { type = NavType.StringType }),
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString(NavRoute.Quiz.ARG) ?: ""
            QuizScreen(
                lessonId = lessonId,
                onQuizComplete = { id ->
                    navController.navigate(NavRoute.QuizResults.create(id)) {
                        popUpTo(NavRoute.Quiz.ROUTE) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        // ── Quiz Results ──────────────────────────────────────────────────────
        composable(
            route = NavRoute.QuizResults.ROUTE,
            arguments = listOf(navArgument(NavRoute.QuizResults.ARG) { type = NavType.StringType }),
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString(NavRoute.QuizResults.ARG) ?: ""
            QuizResultsScreen(
                lessonId = lessonId,
                onNavigateHome = {
                    navController.navigate(NavRoute.Home.route) {
                        popUpTo(NavRoute.Home.route) { inclusive = false }
                    }
                },
                onShareResult = {
                    // Share intent handled via ShareHelper/ShareViewModel at screen level
                },
            )
        }

        // ── Weekly Review ─────────────────────────────────────────────────────
        composable(
            route = NavRoute.WeeklyReview.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = "curiosityengine://quiz/weekly" }
            ),
        ) {
            WeeklyReviewScreen(
                onComplete = {
                    navController.navigate(NavRoute.Home.route) {
                        popUpTo(NavRoute.WeeklyReview.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        // ── Doom Scroll ───────────────────────────────────────────────────────
        composable(
            route = NavRoute.DoomScroll.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = "curiosityengine://doom-scroll" }
            ),
        ) {
            DoomScrollScreen(
                onNavigateToLesson = { lessonId ->
                    navController.navigate(NavRoute.Lesson.create(lessonId))
                },
                onExit = {
                    navController.popBackStack()
                },
            )
        }

        // ── Settings ──────────────────────────────────────────────────────────
        composable(NavRoute.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
