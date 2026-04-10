package com.curiosityengine.app.ui.navigation

sealed class NavRoute(val route: String) {
    data object Splash          : NavRoute("splash")
    data object Auth            : NavRoute("auth")
    data object Onboarding      : NavRoute("onboarding/{userId}") {
        const val ROUTE_WITH_ARG = "onboarding/{userId}"
        const val ARG_USER_ID = "userId"
        fun create(userId: String) = "onboarding/$userId"
    }
    data object Home            : NavRoute("home")
    data object Journal         : NavRoute("journal")
    data object Profile         : NavRoute("profile")

    // Parameterised routes
    data class Lesson(val lessonId: String = "{lessonId}") : NavRoute("lesson/{lessonId}") {
        companion object {
            const val ROUTE = "lesson/{lessonId}"
            const val ARG = "lessonId"
            fun create(id: String) = "lesson/$id"
        }
    }

    data class Quiz(val lessonId: String = "{lessonId}") : NavRoute("quiz/{lessonId}") {
        companion object {
            const val ROUTE = "quiz/{lessonId}"
            const val ARG = "lessonId"
            fun create(id: String) = "quiz/$id"
        }
    }

    data class QuizResults(val resultId: String = "{resultId}") : NavRoute("quiz_results/{resultId}") {
        companion object {
            const val ROUTE = "quiz_results/{resultId}"
            const val ARG = "resultId"
            fun create(id: String) = "quiz_results/$id"
        }
    }

    data object WeeklyReview    : NavRoute("weekly_review")
    data object DoomScroll      : NavRoute("doom_scroll")
    data object Settings        : NavRoute("settings")
}
