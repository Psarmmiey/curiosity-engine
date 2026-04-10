package com.curiosityengine.app.data.repository

import com.curiosityengine.app.data.local.dao.StreakDao
import com.curiosityengine.app.data.model.Streak
import com.curiosityengine.app.data.remote.dto.StreakDto
import com.curiosityengine.app.data.remote.mapper.toDomain
import com.curiosityengine.app.data.remote.mapper.toEntity
import com.curiosityengine.app.util.DateTimeUtil
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakRepositoryImpl @Inject constructor(
    private val streakDao: StreakDao,
    private val supabase: SupabaseClient
) : StreakRepository {

    override fun getStreak(userId: String): Flow<Streak?> =
        streakDao.getStreak(userId).map { it?.toDomain() }

    override suspend fun getStreakOnce(userId: String): Streak? =
        streakDao.getStreakOnce(userId)?.toDomain()

    override suspend fun recordCompletion(userId: String, date: String) {
        val today = DateTimeUtil.parseDate(date)
        val existing = streakDao.getStreakOnce(userId)?.toDomain()
            ?: Streak(userId = userId)

        // --- Chain streak logic ---
        val lastDate = existing.lastCompletedDate?.let { DateTimeUtil.parseDate(it) }
        val newChain = when {
            lastDate == null -> 1
            DateTimeUtil.isToday(lastDate) -> existing.chainStreak          // already counted today
            DateTimeUtil.areConsecutiveDays(lastDate, today) -> existing.chainStreak + 1
            else -> 1                                                         // streak broken
        }
        val newBest = maxOf(newChain, existing.bestStreak)

        // --- Weekly streak logic ---
        val weekStart = DateTimeUtil.currentWeekStart()
        val weekStartStr = DateTimeUtil.formatDate(weekStart)

        // Reset weekly tracking if we're in a new week
        val currentWeekDays: MutableList<Int> = if (existing.weekStartDate != weekStartStr) {
            mutableListOf()
        } else {
            existing.weekDaysDone.toMutableList()
        }

        val todayDow = today.dayOfWeek.value   // 1=Mon … 7=Sun
        if (todayDow !in currentWeekDays) {
            currentWeekDays.add(todayDow)
        }

        val newWeeklyStreak = currentWeekDays.size

        val updatedStreak = existing.copy(
            chainStreak = newChain,
            bestStreak = newBest,
            lastCompletedDate = date,
            weeklyStreak = newWeeklyStreak,
            weekStartDate = weekStartStr,
            weekDaysDone = currentWeekDays.sorted(),
            updatedAt = System.currentTimeMillis()
        )

        streakDao.insertOrUpdate(updatedStreak.toEntity())

        // Best-effort remote sync
        runCatching { pushStreakToSupabase(updatedStreak) }
    }

    override suspend fun syncStreak(userId: String) {
        runCatching {
            val dtos = supabase.postgrest
                .from("streaks")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<StreakDto>()

            dtos.firstOrNull()?.let { dto ->
                val remote = dto.toDomain()
                val local = streakDao.getStreakOnce(userId)?.toDomain()

                // Merge: take the higher chain streak
                val merged = if (local != null && local.chainStreak >= remote.chainStreak) {
                    local.copy(bestStreak = maxOf(local.bestStreak, remote.bestStreak))
                } else {
                    remote
                }
                streakDao.insertOrUpdate(merged.toEntity())
            }
        }
    }

    override suspend fun saveStreak(streak: Streak) {
        streakDao.insertOrUpdate(streak.toEntity())
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    private suspend fun pushStreakToSupabase(streak: Streak) {
        supabase.postgrest
            .from("streaks")
            .upsert(
                value = StreakDto(
                    userId = streak.userId,
                    chainStreak = streak.chainStreak,
                    bestStreak = streak.bestStreak,
                    lastCompletedDate = streak.lastCompletedDate,
                    weeklyStreak = streak.weeklyStreak,
                    weekStartDate = streak.weekStartDate,
                    weekDaysDone = streak.weekDaysDone,
                    updatedAt = streak.updatedAt
                )
            )
    }
}
