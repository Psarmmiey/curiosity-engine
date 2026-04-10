package com.curiosityengine.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String? = null,
    val email: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("preferred_categories") val preferredCategories: List<String> = emptyList(),
    @SerialName("daily_target") val dailyTarget: Int = 1,
    @SerialName("reminder_time") val reminderTime: String = "08:00",
    @SerialName("email_daily_nudge") val emailDailyNudge: Boolean = false,
    @SerialName("email_weekly_digest") val emailWeeklyDigest: Boolean = true,
    @SerialName("lesson_model") val lessonModel: String = "gemini-2.5-pro",
    @SerialName("images_enabled") val imagesEnabled: Boolean = true,
    @SerialName("video_enabled") val videoEnabled: Boolean = true
)
