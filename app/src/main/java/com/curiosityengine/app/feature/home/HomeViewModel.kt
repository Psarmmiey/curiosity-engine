package com.curiosityengine.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curiosityengine.app.data.repository.LessonRepository
import com.curiosityengine.app.data.repository.StreakRepository
import com.curiosityengine.app.data.repository.UserRepository
import com.curiosityengine.app.util.DateTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val streakRepository: StreakRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun refresh() {
        load()
    }

    fun getUserId(): String? = _uiState.value.userPrefs?.userId

    private fun load() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            try {
                // Get user prefs first to resolve userId
                val prefs = userRepository.getUserPrefsOnce("") ?: run {
                    _uiState.value = HomeUiState(isLoading = false, error = "User not found")
                    return@launch
                }
                val userId = prefs.userId
                val todayStr = DateTimeUtil.formatDate(DateTimeUtil.today())

                // Fetch today's lesson (network + cache)
                lessonRepository.fetchAndCacheTodaysLesson(userId, todayStr)

                // Sync streak
                streakRepository.syncStreak(userId)

                // Now observe combined reactive state
                combine(
                    lessonRepository.getLessonForDate(todayStr, userId),
                    lessonRepository.getLessonsForUser(userId),
                    streakRepository.getStreak(userId),
                    userRepository.getUserPrefs(userId),
                ) { todayLesson, allLessons, streak, latestPrefs ->
                    val sevenDaysAgo = DateTimeUtil.today().minusDays(7)
                    val recentLessons = allLessons
                        .filter { lesson ->
                            try {
                                val lessonDate = DateTimeUtil.parseDate(lesson.date)
                                lessonDate.isAfter(sevenDaysAgo) && lesson.date != todayStr
                            } catch (e: Exception) {
                                false
                            }
                        }
                        .take(5)

                    val dailyProgressMinutes = if (todayLesson?.isRead == true) {
                        todayLesson.estimatedReadMinutes
                    } else {
                        0
                    }

                    HomeUiState(
                        isLoading = false,
                        todayLesson = todayLesson,
                        recentLessons = recentLessons,
                        streak = streak,
                        userPrefs = latestPrefs ?: prefs,
                        dailyProgressMinutes = dailyProgressMinutes,
                        error = null,
                    )
                }
                    .catch { e ->
                        Timber.e(e, "Error observing home data")
                        emit(HomeUiState(isLoading = false, error = e.message ?: "Unknown error"))
                    }
                    .collect { state ->
                        _uiState.value = state
                    }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load home screen data")
                _uiState.value = HomeUiState(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }
}

// Extension to safely access estimatedReadMinutes from Lesson
private val com.curiosityengine.app.data.model.Lesson.estimatedReadMinutes: Int
    get() = blocks.size * 2 // rough approximation: 2 min per block
