package com.curiosityengine.app.feature.home

import com.curiosityengine.app.data.model.Lesson
import com.curiosityengine.app.data.model.Streak
import com.curiosityengine.app.data.model.UserPrefs

data class HomeUiState(
    val isLoading: Boolean = true,
    val todayLesson: Lesson? = null,
    val recentLessons: List<Lesson> = emptyList(),
    val streak: Streak? = null,
    val userPrefs: UserPrefs? = null,
    val dailyProgressMinutes: Int = 0,
    val error: String? = null
)
