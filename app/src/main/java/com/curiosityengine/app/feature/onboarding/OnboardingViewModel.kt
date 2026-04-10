package com.curiosityengine.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curiosityengine.app.data.model.UserPrefs
import com.curiosityengine.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun nextStep() {
        _state.update { current ->
            current.copy(currentStep = (current.currentStep + 1).coerceAtMost(3))
        }
    }

    fun prevStep() {
        _state.update { current ->
            current.copy(currentStep = (current.currentStep - 1).coerceAtLeast(0))
        }
    }

    fun toggleCategory(category: String) {
        _state.update { current ->
            val updated = if (category in current.selectedCategories) {
                current.selectedCategories - category
            } else {
                current.selectedCategories + category
            }
            current.copy(selectedCategories = updated)
        }
    }

    fun setDailyTarget(minutes: Int) {
        _state.update { it.copy(dailyTargetMinutes = minutes) }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        _state.update { it.copy(notificationEnabled = enabled) }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        _state.update { it.copy(notificationHour = hour, notificationMinute = minute) }
    }

    fun completeOnboarding(userId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val current = _state.value
                val reminderTime = "%02d:%02d".format(current.notificationHour, current.notificationMinute)

                val prefs = UserPrefs(
                    userId = userId,
                    preferredCategories = current.selectedCategories.toList(),
                    dailyTarget = current.dailyTargetMinutes,
                    reminderTime = reminderTime,
                    emailDailyNudge = current.notificationEnabled,
                )

                userRepository.saveUserPrefs(prefs)
                onComplete()
            } catch (e: Exception) {
                Timber.e(e, "OnboardingViewModel: failed to save prefs")
                // Still proceed to home even if save fails — non-blocking
                onComplete()
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
