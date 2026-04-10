package com.curiosityengine.app.feature.streak

import com.curiosityengine.app.data.model.Streak
import com.curiosityengine.app.data.repository.StreakRepository
import com.curiosityengine.app.util.DateTimeUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakManager @Inject constructor(
    private val streakRepository: StreakRepository
) {

    /**
     * Expose the current streak for [userId] as a non-null [Flow].
     * Emits a default [Streak] when there is no persisted data yet.
     */
    fun currentStreak(userId: String): Flow<Streak> =
        streakRepository.getStreak(userId).map { it ?: Streak(userId = userId) }

    /**
     * Record that [userId] completed an activity today.
     *
     * Reads the current streak, applies [StreakCalculator] for both chain and
     * weekly calculations, persists the result via [StreakRepository.recordCompletion].
     */
    suspend fun recordActivity(userId: String) {
        val today = DateTimeUtil.today()
        val todayStr = DateTimeUtil.formatDate(today)

        val existing = streakRepository.getStreakOnce(userId) ?: Streak(userId = userId)

        // Apply business logic via pure calculator
        val afterChain = StreakCalculator.computeChainStreak(existing, today)
        val afterWeekly = StreakCalculator.computeWeeklyStreak(afterChain, today)

        // Persist using the repository (which handles local + remote sync)
        streakRepository.saveStreak(
            afterWeekly.copy(
                lastCompletedDate = todayStr,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * If today is Monday and [weekDaysDone] contains entries from a previous
     * week, reset the weekly streak fields so the new week starts clean.
     *
     * Note: [StreakCalculator.computeWeeklyStreak] already handles week
     * transitions on every [recordActivity] call.  This method is intended for
     * cases where the app opens on a Monday before the user has completed any
     * activity (e.g. a background job / startup check).
     */
    suspend fun checkAndResetWeeklyStreak(userId: String) {
        val today = DateTimeUtil.today()
        if (!StreakCalculator.shouldResetWeeklyStreak(today)) return

        val existing = streakRepository.getStreakOnce(userId) ?: return

        val currentWeekStart = DateTimeUtil.formatDate(DateTimeUtil.currentWeekStart())
        if (existing.weekStartDate == currentWeekStart) return  // already up-to-date

        // It is Monday and the stored week start is from a previous week — reset
        streakRepository.saveStreak(
            existing.copy(
                weeklyStreak = 0,
                weekStartDate = currentWeekStart,
                weekDaysDone = emptyList(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
