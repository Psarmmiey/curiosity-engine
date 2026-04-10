package com.curiosityengine.app.feature.profile

import com.curiosityengine.app.data.model.Streak

data class ProfileUiState(
    val isLoading: Boolean = true,
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val streak: Streak? = null,
    val totalLessonsCompleted: Int = 0,
    val badges: List<Badge> = emptyList(),
    val error: String? = null,
)

data class Badge(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
)
