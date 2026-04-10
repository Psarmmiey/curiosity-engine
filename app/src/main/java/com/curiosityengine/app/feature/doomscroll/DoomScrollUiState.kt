package com.curiosityengine.app.feature.doomscroll

import com.curiosityengine.app.data.model.Lesson

data class DoomScrollUiState(
    val lessons: List<Lesson> = emptyList(),
    val currentIndex: Int = 0,
    val isLoadingMore: Boolean = false,
    val error: String? = null
)
