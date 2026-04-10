package com.curiosityengine.app.data.model

data class UserPrefs(
    val userId: String,
    val displayName: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val preferredCategories: List<String> = emptyList(),
    val dailyTarget: Int = 1,
    val reminderTime: String = "08:00",
    val emailDailyNudge: Boolean = false,
    val emailWeeklyDigest: Boolean = true,
    val lessonModel: String = "gemini-2.5-pro",
    val imagesEnabled: Boolean = true,
    val videoEnabled: Boolean = true
)
