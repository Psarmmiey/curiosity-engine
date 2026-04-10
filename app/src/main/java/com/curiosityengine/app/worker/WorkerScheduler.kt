package com.curiosityengine.app.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkerScheduler @Inject constructor() {

    fun scheduleAll(context: Context) {
        scheduleLessonPrefetch(context)
    }

    fun scheduleLessonPrefetch(context: Context) {
        val initialDelayMs = computeInitialDelayToNext6Am()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<LessonPrefetchWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(LessonPrefetchWorker.TAG)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                LessonPrefetchWorker.TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )

        Timber.d("WorkerScheduler: LessonPrefetchWorker scheduled (initialDelay=${initialDelayMs}ms)")
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
        Timber.d("WorkerScheduler: all workers cancelled")
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Computes the milliseconds until the next 6:00 AM in the device's local timezone.
     * If it is currently past 6 AM, returns the delay to 6 AM tomorrow.
     */
    private fun computeInitialDelayToNext6Am(): Long {
        val now = LocalDateTime.now(ZoneId.systemDefault())
        val todayAt6Am = now.toLocalDate().atTime(6, 0, 0)

        val target = if (now.isBefore(todayAt6Am)) {
            todayAt6Am
        } else {
            todayAt6Am.plusDays(1)
        }

        val nowEpochMs = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val targetEpochMs = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        return (targetEpochMs - nowEpochMs).coerceAtLeast(0L)
    }
}
