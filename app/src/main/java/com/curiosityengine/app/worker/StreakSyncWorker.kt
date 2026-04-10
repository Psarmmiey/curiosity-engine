package com.curiosityengine.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.curiosityengine.app.data.repository.StreakRepository
import com.curiosityengine.app.data.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class StreakSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val streakRepository: StreakRepository,
    private val userRepository: UserRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val userId = inputData.getString(KEY_USER_ID)
                ?: run {
                    Timber.w("StreakSyncWorker: no userId in input, aborting")
                    return Result.failure()
                }

            Timber.d("StreakSyncWorker: syncing streak for userId=$userId")
            streakRepository.syncStreak(userId)
            Timber.d("StreakSyncWorker: streak sync complete")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "StreakSyncWorker: failed to sync streak")
            Result.retry()
        }
    }

    companion object {
        const val TAG = "streak_sync"
        const val KEY_USER_ID = "user_id"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<StreakSyncWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    TAG,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }
    }
}
