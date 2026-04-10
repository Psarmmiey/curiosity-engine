package com.curiosityengine.app.feature.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curiosityengine.app.data.repository.LessonRepository
import com.curiosityengine.app.data.repository.StreakRepository
import com.curiosityengine.app.util.DateTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val streakRepository: StreakRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LessonUiState>(LessonUiState.Loading)
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    private val _showCompletionOverlay = MutableStateFlow(false)
    val showCompletionOverlay: StateFlow<Boolean> = _showCompletionOverlay.asStateFlow()

    // Tracks in-flight completion so we don't double-call markComplete
    private var completionInProgress = false

    fun loadLesson(lessonId: String) {
        viewModelScope.launch {
            _uiState.value = LessonUiState.Loading
            try {
                // Determine how to look up the lesson
                val lesson = if (lessonId == "today") {
                    val todayStr = DateTimeUtil.formatDate(DateTimeUtil.today())
                    // We don't have userId here; use getLessonByIdOnce as a fallback,
                    // then try observing via date with empty userId (repository handles gracefully)
                    lessonRepository.getLessonById(todayStr).first()
                        ?: lessonRepository.getLessonByIdOnce(todayStr)
                } else {
                    lessonRepository.getLessonById(lessonId).first()
                        ?: lessonRepository.getLessonByIdOnce(lessonId)
                }

                if (lesson != null) {
                    _uiState.value = LessonUiState.Success(
                        lesson = lesson,
                        isCompleted = lesson.isRead,
                    )
                    // If already read, still allow overlay to be dismissed silently
                    if (lesson.isRead) {
                        completionInProgress = true
                    }
                } else {
                    _uiState.value = LessonUiState.Error("Lesson not found.")
                }

                // Continue observing for updates (read state, quiz score changes)
                val flow = if (lessonId == "today") {
                    val todayStr = DateTimeUtil.formatDate(DateTimeUtil.today())
                    lessonRepository.getLessonById(todayStr)
                } else {
                    lessonRepository.getLessonById(lessonId)
                }
                flow.collect { updated ->
                    if (updated != null) {
                        val current = _uiState.value
                        val wasCompleted = (current as? LessonUiState.Success)?.isCompleted ?: updated.isRead
                        _uiState.value = LessonUiState.Success(
                            lesson = updated,
                            scrollProgress = (current as? LessonUiState.Success)?.scrollProgress ?: 0f,
                            isCompleted = wasCompleted || updated.isRead,
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load lesson: $lessonId")
                _uiState.value = LessonUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateScrollProgress(progress: Float) {
        val current = _uiState.value as? LessonUiState.Success ?: return
        _uiState.value = current.copy(scrollProgress = progress)

        if (progress >= 0.9f && !current.isCompleted && !completionInProgress) {
            markComplete()
        }
    }

    fun markComplete() {
        val current = _uiState.value as? LessonUiState.Success ?: return
        if (completionInProgress) return
        completionInProgress = true

        val lesson = current.lesson
        viewModelScope.launch {
            try {
                lessonRepository.markAsRead(lesson.id)
                lesson.userId?.let { uid ->
                    streakRepository.recordCompletion(uid, lesson.date)
                }
                _uiState.value = current.copy(isCompleted = true)
                _showCompletionOverlay.value = true
            } catch (e: Exception) {
                Timber.e(e, "Failed to mark lesson complete: ${lesson.id}")
                completionInProgress = false
            }
        }
    }

    fun dismissCompletionOverlay() {
        _showCompletionOverlay.value = false
    }
}
