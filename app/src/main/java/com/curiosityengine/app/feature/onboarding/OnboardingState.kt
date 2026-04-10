package com.curiosityengine.app.feature.onboarding

data class OnboardingState(
    val currentStep: Int = 0,          // 0..3
    val selectedCategories: Set<String> = emptySet(),
    val dailyTargetMinutes: Int = 10,
    val notificationEnabled: Boolean = true,
    val notificationHour: Int = 8,
    val notificationMinute: Int = 0,
    val isLoading: Boolean = false,
)
