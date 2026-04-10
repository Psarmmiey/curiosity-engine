package com.curiosityengine.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curiosityengine.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var userId: String = ""

    init {
        loadPrefs()
    }

    private fun loadPrefs() {
        viewModelScope.launch {
            try {
                val prefs = userRepository.getUserPrefsOnce("") ?: run {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    return@launch
                }
                userId = prefs.userId

                // Parse reminder time "HH:mm"
                val (hour, minute) = parseReminderTime(prefs.reminderTime)

                _uiState.value = SettingsUiState(
                    isLoading = false,
                    dailyTargetMinutes = dailyTargetToMinutes(prefs.dailyTarget),
                    selectedCategories = prefs.preferredCategories.toSet(),
                    notificationEnabled = true, // stored as reminder time presence; default on
                    notificationHour = hour,
                    notificationMinute = minute,
                    preferredModel = prefs.lessonModel,
                    enableImages = prefs.imagesEnabled,
                    enableVideos = prefs.videoEnabled,
                    emailDailyNudge = prefs.emailDailyNudge,
                    isSaving = false,
                )
            } catch (e: Exception) {
                Timber.e(e, "SettingsViewModel: failed to load prefs")
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun setDailyTarget(minutes: Int) {
        _uiState.value = _uiState.value.copy(dailyTargetMinutes = minutes)
    }

    fun toggleCategory(cat: String) {
        val current = _uiState.value.selectedCategories.toMutableSet()
        if (current.contains(cat)) current.remove(cat) else current.add(cat)
        _uiState.value = _uiState.value.copy(selectedCategories = current)
    }

    fun setNotificationEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationEnabled = enabled)
    }

    fun setNotificationTime(h: Int, m: Int) {
        _uiState.value = _uiState.value.copy(notificationHour = h, notificationMinute = m)
    }

    fun setPreferredModel(model: String) {
        _uiState.value = _uiState.value.copy(preferredModel = model)
    }

    fun toggleImages() {
        _uiState.value = _uiState.value.copy(enableImages = !_uiState.value.enableImages)
    }

    fun toggleVideos() {
        _uiState.value = _uiState.value.copy(enableVideos = !_uiState.value.enableVideos)
    }

    fun setEmailNudge(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(emailDailyNudge = enabled)
    }

    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val state = _uiState.value
                val currentPrefs = userRepository.getUserPrefsOnce(userId)
                    ?: com.curiosityengine.app.data.model.UserPrefs(userId = userId)

                val reminderTime = "%02d:%02d".format(state.notificationHour, state.notificationMinute)
                val updatedPrefs = currentPrefs.copy(
                    dailyTarget = minutesToDailyTarget(state.dailyTargetMinutes),
                    preferredCategories = state.selectedCategories.toList(),
                    reminderTime = reminderTime,
                    lessonModel = state.preferredModel,
                    imagesEnabled = state.enableImages,
                    videoEnabled = state.enableVideos,
                    emailDailyNudge = state.emailDailyNudge,
                )
                userRepository.updateUserPrefs(updatedPrefs)
                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                Timber.e(e, "SettingsViewModel: failed to save prefs")
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    private fun parseReminderTime(reminderTime: String): Pair<Int, Int> {
        return try {
            val parts = reminderTime.split(":")
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            Pair(8, 0)
        }
    }

    /** UserPrefs.dailyTarget stores lessons-per-day, we store minutes in UI. */
    private fun dailyTargetToMinutes(dailyTarget: Int): Int = when (dailyTarget) {
        1 -> 5
        2 -> 10
        3 -> 15
        6 -> 30
        12 -> 60
        else -> (dailyTarget * 5).coerceIn(5, 60)
    }

    private fun minutesToDailyTarget(minutes: Int): Int = when (minutes) {
        5 -> 1
        10 -> 2
        15 -> 3
        30 -> 6
        60 -> 12
        else -> (minutes / 5).coerceIn(1, 12)
    }
}
