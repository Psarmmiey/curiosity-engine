package com.curiosityengine.app.widget

import androidx.glance.state.GlanceStateDefinition
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

data class CuriosityWidgetState(
    val topic: String = "Loading...",
    val category: String = "",
    val emoji: String = "\uD83E\uDDE0",
    val isRead: Boolean = false,
    val quizDone: Boolean = false,
    val chainStreak: Int = 0,
    val dailyProgressMinutes: Int = 0,
    val dailyTargetMinutes: Int = 10,
) {
    companion object {
        val KEY_TOPIC = stringPreferencesKey("widget_topic")
        val KEY_CATEGORY = stringPreferencesKey("widget_category")
        val KEY_EMOJI = stringPreferencesKey("widget_emoji")
        val KEY_IS_READ = booleanPreferencesKey("widget_is_read")
        val KEY_QUIZ_DONE = booleanPreferencesKey("widget_quiz_done")
        val KEY_CHAIN_STREAK = intPreferencesKey("widget_chain_streak")
        val KEY_DAILY_PROGRESS = intPreferencesKey("widget_daily_progress")
        val KEY_DAILY_TARGET = intPreferencesKey("widget_daily_target")

        fun fromPrefs(prefs: Preferences) = CuriosityWidgetState(
            topic = prefs[KEY_TOPIC] ?: "Loading...",
            category = prefs[KEY_CATEGORY] ?: "",
            emoji = prefs[KEY_EMOJI] ?: "\uD83E\uDDE0",
            isRead = prefs[KEY_IS_READ] ?: false,
            quizDone = prefs[KEY_QUIZ_DONE] ?: false,
            chainStreak = prefs[KEY_CHAIN_STREAK] ?: 0,
            dailyProgressMinutes = prefs[KEY_DAILY_PROGRESS] ?: 0,
            dailyTargetMinutes = prefs[KEY_DAILY_TARGET] ?: 10,
        )
    }
}
