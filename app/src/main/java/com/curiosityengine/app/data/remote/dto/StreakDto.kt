package com.curiosityengine.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreakDto(
    @SerialName("user_id") val userId: String,
    @SerialName("chain_streak") val chainStreak: Int = 0,
    @SerialName("best_streak") val bestStreak: Int = 0,
    @SerialName("last_completed_date") val lastCompletedDate: String? = null,
    @SerialName("weekly_streak") val weeklyStreak: Int = 0,
    @SerialName("week_start_date") val weekStartDate: String? = null,
    @SerialName("week_days_done") val weekDaysDone: List<Int> = emptyList(),
    @SerialName("updated_at") val updatedAt: Long = 0L
)
