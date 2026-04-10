package com.curiosityengine.app.feature.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curiosityengine.app.data.repository.LessonRepository
import com.curiosityengine.app.data.repository.UserRepository
import com.curiosityengine.app.util.DateTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    private var userId: String = ""

    init {
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            try {
                val prefs = userRepository.getUserPrefsOnce("") ?: run {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "User not found")
                    return@launch
                }
                userId = prefs.userId
                observeLessons(DateTimeUtil.currentWeekStart())
            } catch (e: Exception) {
                Timber.e(e, "JournalViewModel: failed to load initial data")
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }

    private fun observeLessons(weekStart: LocalDate) {
        viewModelScope.launch {
            lessonRepository.getLessonsForUser(userId)
                .catch { e ->
                    Timber.e(e, "JournalViewModel: error observing lessons")
                    emit(emptyList())
                }
                .collect { all ->
                    val weekEnd = weekStart.plusDays(6)
                    val thisWeek = all.filter { lesson ->
                        try {
                            val date = DateTimeUtil.parseDate(lesson.date)
                            !date.isBefore(weekStart) && !date.isAfter(weekEnd)
                        } catch (e: Exception) {
                            false
                        }
                    }.sortedByDescending { it.date }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedWeekStart = weekStart,
                        lessonsThisWeek = thisWeek,
                        allLessons = all,
                        error = null,
                    )
                }
        }
    }

    fun selectWeek(weekStart: LocalDate) {
        val today = DateTimeUtil.today()
        val clampedWeekStart = if (weekStart.isAfter(DateTimeUtil.currentWeekStart())) {
            DateTimeUtil.currentWeekStart()
        } else {
            weekStart
        }
        _uiState.value = _uiState.value.copy(isLoading = true, selectedWeekStart = clampedWeekStart)
        observeLessons(clampedWeekStart)
    }

    fun prevWeek() {
        val newStart = _uiState.value.selectedWeekStart.minusWeeks(1)
        selectWeek(newStart)
    }

    fun nextWeek() {
        val currentWeekStart = DateTimeUtil.currentWeekStart()
        val candidateStart = _uiState.value.selectedWeekStart.plusWeeks(1)
        // Don't allow navigating past the current week
        if (!candidateStart.isAfter(currentWeekStart)) {
            selectWeek(candidateStart)
        }
    }
}
