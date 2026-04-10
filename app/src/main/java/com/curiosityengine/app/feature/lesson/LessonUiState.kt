package com.curiosityengine.app.feature.lesson

import com.curiosityengine.app.data.model.Lesson

sealed class LessonUiState {
    data object Loading : LessonUiState()
    data class Success(
        val lesson: Lesson,
        val scrollProgress: Float = 0f,
        val isCompleted: Boolean = false,
    ) : LessonUiState()
    data class Error(val message: String) : LessonUiState()
}
