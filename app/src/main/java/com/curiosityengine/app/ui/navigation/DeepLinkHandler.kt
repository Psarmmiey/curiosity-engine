package com.curiosityengine.app.ui.navigation

import android.net.Uri

object DeepLinkHandler {

    /**
     * Maps curiosityengine:// deep link URIs to NavRoutes.
     * Returns null if the URI is unrecognised.
     */
    fun resolve(uri: Uri?): String? {
        uri ?: return null
        if (uri.scheme != "curiosityengine") return null

        return when (uri.host) {
            "lesson" -> {
                val date = uri.lastPathSegment
                if (date != null) NavRoute.Lesson.create(date) else NavRoute.Home.route
            }
            "quiz" -> {
                val segment = uri.lastPathSegment
                if (segment == "weekly") NavRoute.WeeklyReview.route
                else if (segment != null) NavRoute.Quiz.create(segment)
                else NavRoute.Home.route
            }
            "doom-scroll" -> NavRoute.DoomScroll.route
            "journal"     -> NavRoute.Journal.route
            "home"        -> NavRoute.Home.route
            else          -> NavRoute.Home.route
        }
    }
}
