package com.curiosityengine.app.feature.share

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curiosityengine.app.data.repository.LessonRepository
import com.curiosityengine.app.data.repository.StreakRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val streakRepository: StreakRepository,
) : ViewModel() {

    private val _isSharing = MutableStateFlow(false)
    val isSharing: StateFlow<Boolean> = _isSharing.asStateFlow()

    /**
     * Fetches the lesson by [lessonId], renders a [ShareCardType.LessonCard], and shares it.
     */
    fun shareLesson(context: Context, lessonId: String) {
        viewModelScope.launch {
            _isSharing.value = true
            try {
                val lesson = lessonRepository.getLessonByIdOnce(lessonId)
                    ?: run {
                        Timber.w("ShareViewModel: lesson $lessonId not found")
                        return@launch
                    }
                val bitmap = ShareCardRenderer.render(context, ShareCardType.LessonCard(lesson))
                ShareHelper.shareImage(context, bitmap, lesson.topic)
            } catch (e: Exception) {
                Timber.e(e, "ShareViewModel: shareLesson failed")
            } finally {
                _isSharing.value = false
            }
        }
    }

    /**
     * Fetches the lesson by [lessonId], renders a [ShareCardType.QuizScoreCard], and shares it.
     */
    fun shareQuizResult(context: Context, lessonId: String, score: Int, total: Int) {
        viewModelScope.launch {
            _isSharing.value = true
            try {
                val lesson = lessonRepository.getLessonByIdOnce(lessonId)
                    ?: run {
                        Timber.w("ShareViewModel: lesson $lessonId not found for quiz share")
                        return@launch
                    }
                val bitmap = ShareCardRenderer.render(
                    context,
                    ShareCardType.QuizScoreCard(lesson, score, total)
                )
                ShareHelper.shareImage(context, bitmap, "Quiz: ${lesson.topic}")
            } catch (e: Exception) {
                Timber.e(e, "ShareViewModel: shareQuizResult failed")
            } finally {
                _isSharing.value = false
            }
        }
    }

    /**
     * Fetches the current streak, renders a [ShareCardType.StreakMilestoneCard], and shares it.
     *
     * @param userId  the authenticated user's ID — needed to resolve the streak.
     * @param milestone  the milestone value (7, 14, 30, 60, or 100).
     */
    fun shareStreakMilestone(context: Context, userId: String, milestone: Int) {
        viewModelScope.launch {
            _isSharing.value = true
            try {
                val streak = streakRepository.getStreak(userId).first()
                    ?: run {
                        Timber.w("ShareViewModel: no streak found for user $userId")
                        return@launch
                    }
                val bitmap = ShareCardRenderer.render(
                    context,
                    ShareCardType.StreakMilestoneCard(milestone, streak)
                )
                ShareHelper.shareImage(context, bitmap, "$milestone Day Streak!")
            } catch (e: Exception) {
                Timber.e(e, "ShareViewModel: shareStreakMilestone failed")
            } finally {
                _isSharing.value = false
            }
        }
    }

    /**
     * Fetches the current streak, renders a [ShareCardType.WeeklyStatsCard], and shares it.
     *
     * @param userId            the authenticated user's ID.
     * @param lessonsCompleted  number of lessons completed this week.
     * @param xpEarned          total XP earned this week.
     */
    fun shareWeeklyStats(context: Context, userId: String, lessonsCompleted: Int, xpEarned: Int) {
        viewModelScope.launch {
            _isSharing.value = true
            try {
                val streak = streakRepository.getStreak(userId).first()
                    ?: run {
                        Timber.w("ShareViewModel: no streak found for user $userId")
                        return@launch
                    }
                val bitmap = ShareCardRenderer.render(
                    context,
                    ShareCardType.WeeklyStatsCard(lessonsCompleted, xpEarned, streak)
                )
                ShareHelper.shareImage(context, bitmap, "My Week on Curiosity Engine")
            } catch (e: Exception) {
                Timber.e(e, "ShareViewModel: shareWeeklyStats failed")
            } finally {
                _isSharing.value = false
            }
        }
    }
}
