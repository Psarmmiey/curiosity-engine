package com.curiosityengine.app.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.curiosityengine.app.data.repository.LessonRepository
import com.curiosityengine.app.data.repository.StreakRepository
import com.curiosityengine.app.data.repository.UserRepository
import com.curiosityengine.app.util.DateTimeUtil
import com.curiosityengine.app.widget.CuriosityWidget
import com.curiosityengine.app.widget.CuriosityWidgetState
import com.curiosityengine.app.widget.CuriosityWidgetStateDefinition
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val lessonRepository: LessonRepository,
    private val streakRepository: StreakRepository,
    private val userRepository: UserRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userPrefs = userRepository.getUserPrefs().firstOrNull()?.getOrNull()
            val userId = userPrefs?.userId ?: return Result.failure()

            val todayLesson = lessonRepository
                .getLessonByDate(userId, DateTimeUtil.today())
                .firstOrNull()?.getOrNull()

            val streak = streakRepository.getStreak(userId).firstOrNull()?.getOrNull()

            val widgetState = CuriosityWidgetState(
                topic = todayLesson?.topic ?: "Ready to learn?",
                category = todayLesson?.category ?: "",
                emoji = todayLesson?.emoji ?: "🧠",
                isRead = todayLesson?.isRead ?: false,
                quizDone = (todayLesson?.quizScore ?: -1) >= 0,
                chainStreak = streak?.chainStreak ?: 0,
                dailyProgressMinutes = if (todayLesson?.isRead == true) userPrefs.dailyTargetMinutes else 0,
                dailyTargetMinutes = userPrefs.dailyTargetMinutes
            )

            val manager = GlanceAppWidgetManager(applicationContext)
            val widget = CuriosityWidget()

            manager.getGlanceIds(CuriosityWidget::class.java).forEach { glanceId ->
                updateAppWidgetState(applicationContext, CuriosityWidgetStateDefinition, glanceId) {
                    widgetState
                }
                widget.update(applicationContext, glanceId)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val TAG = "widget_update"

        fun schedule(context: Context) {
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .addTag(TAG)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
