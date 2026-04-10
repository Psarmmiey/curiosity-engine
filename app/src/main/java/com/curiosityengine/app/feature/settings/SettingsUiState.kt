package com.curiosityengine.app.feature.settings

data class SettingsUiState(
    val isLoading: Boolean = true,
    val dailyTargetMinutes: Int = 10,
    val selectedCategories: Set<String> = emptySet(),
    val notificationEnabled: Boolean = true,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0,
    val preferredModel: String = "gemini-2.5-pro",
    val enableImages: Boolean = true,
    val enableVideos: Boolean = true,
    val emailDailyNudge: Boolean = false,
    val isSaving: Boolean = false,
)
