package com.curiosityengine.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.curiosityengine.app.data.repository.LessonRepository
import com.curiosityengine.app.data.repository.UserRepository
import com.curiosityengine.app.network.SupabaseEdgeFunctions
import com.curiosityengine.app.util.DateTimeUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import timber.log.Timber

@HiltWorker
class LessonPrefetchWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val lessonRepository: LessonRepository,
    private val userRepository: UserRepository,
    private val supabaseClient: SupabaseClient
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userPrefs = userRepository.getUserPrefsOnce(
                userId = inputData.getString(KEY_USER_ID)
                    ?: run {
                        Timber.w("LessonPrefetchWorker: no userId in input, aborting")
                        return Result.failure()
                    }
            ) ?: run {
                Timber.w("LessonPrefetchWorker: no user prefs found, aborting")
                return Result.failure()
            }

            val userId = userPrefs.userId
            val today = DateTimeUtil.today()
            val todayStr = DateTimeUtil.formatDate(today)

            // Skip generation if today's lesson already exists locally
            val existingLesson = lessonRepository.getLessonForDateOnce(
                date = todayStr,
                userId = userId
            )
            if (existingLesson != null) {
                Timber.d("LessonPrefetchWorker: today's lesson already cached, skipping")
                return Result.success()
            }

            val category = userPrefs.preferredCategories.firstOrNull() ?: "Science"
            val model = userPrefs.lessonModel

            Timber.d("LessonPrefetchWorker: generating lesson for $userId, category=$category, date=$todayStr")

            SupabaseEdgeFunctions.generateLesson(
                client = supabaseClient,
                userId = userId,
                category = category,
                date = todayStr,
                model = model
            )

            // Fetch and cache the generated lesson into Room
            lessonRepository.fetchAndCacheTodaysLesson(userId = userId, date = todayStr)

            Timber.d("LessonPrefetchWorker: lesson prefetch complete")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "LessonPrefetchWorker: transient failure, will retry")
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val TAG = "lesson_prefetch"
        const val KEY_USER_ID = "user_id"
        private const val MAX_RETRIES = 3
    }
}
