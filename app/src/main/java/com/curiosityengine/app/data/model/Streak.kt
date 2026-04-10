package com.curiosityengine.app.data.model

data class Streak(
    val userId: String,
    val chainStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDate: String? = null,
    val weeklyStreak: Int = 0,
    val weekStartDate: String? = null,
    val weekDaysDone: List<Int> = emptyList(),
    val updatedAt: Long = 0L
)
