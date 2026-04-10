package com.curiosityengine.app.feature.journal

import com.curiosityengine.app.data.model.Lesson
import com.curiosityengine.app.util.DateTimeUtil
import java.time.LocalDate

data class JournalUiState(
    val isLoading: Boolean = true,
    val selectedWeekStart: LocalDate = DateTimeUtil.currentWeekStart(),
    val lessonsThisWeek: List<Lesson> = emptyList(),
    val allLessons: List<Lesson> = emptyList(),
    val error: String? = null,
)
