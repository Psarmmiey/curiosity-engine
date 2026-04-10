package com.curiosityengine.app.feature.streak

import com.curiosityengine.app.data.model.Streak
import com.curiosityengine.app.util.DateTimeUtil
import java.time.DayOfWeek
import java.time.LocalDate

object StreakCalculator {

    /**
     * Given the current streak and a new activity date (ISO yyyy-MM-dd string),
     * compute and return an updated [Streak] with chain streak recalculated.
     *
     * Rules:
     * - null lastCompletedDate → chainStreak = 1
     * - same day as lastCompletedDate → no change
     * - consecutive day after lastCompletedDate → chainStreak + 1
     * - gap > 1 day → chainStreak reset to 1
     * - bestStreak is always max(bestStreak, newChainStreak)
     */
    fun computeChainStreak(current: Streak, activityDate: LocalDate): Streak {
        val lastDate = current.lastCompletedDate?.let { DateTimeUtil.parseDate(it) }
        val newChain = when {
            lastDate == null -> 1
            lastDate == activityDate -> current.chainStreak  // same day — no change
            DateTimeUtil.areConsecutiveDays(lastDate, activityDate) -> current.chainStreak + 1
            else -> 1  // gap > 1 day — reset
        }
        val newBest = maxOf(current.bestStreak, newChain)
        return current.copy(chainStreak = newChain, bestStreak = newBest)
    }

    /**
     * Given the current streak and a new activity date, compute and return an
     * updated [Streak] with weekly streak recalculated.
     *
     * Weekly tracking uses day-of-week integers (1=Mon … 7=Sun) stored in
     * [Streak.weekDaysDone].  A new Mon-Sun week resets the list.
     *
     * [Streak.weeklyStreak] = number of distinct days completed in the current week.
     */
    fun computeWeeklyStreak(current: Streak, activityDate: LocalDate): Streak {
        val weekStart = DateTimeUtil.currentWeekStart()
        val weekStartStr = DateTimeUtil.formatDate(weekStart)

        // If we have moved into a new week, start fresh
        val existingWeekDays: MutableList<Int> =
            if (current.weekStartDate != weekStartStr) mutableListOf()
            else current.weekDaysDone.toMutableList()

        val todayDow = activityDate.dayOfWeek.value  // 1=Mon … 7=Sun
        if (todayDow !in existingWeekDays) {
            existingWeekDays.add(todayDow)
        }

        return current.copy(
            weeklyStreak = existingWeekDays.size,
            weekStartDate = weekStartStr,
            weekDaysDone = existingWeekDays.sorted()
        )
    }

    /**
     * Returns true if the chain streak was broken — i.e., the gap between
     * [lastActivityDate] and [activityDate] is more than 1 calendar day.
     * Returns false if [lastActivityDate] is null (no prior streak to break).
     */
    fun isStreakBroken(lastActivityDate: LocalDate?, activityDate: LocalDate): Boolean {
        if (lastActivityDate == null) return false
        return !DateTimeUtil.areConsecutiveDays(lastActivityDate, activityDate) &&
            lastActivityDate != activityDate
    }

    /**
     * Returns true if [activityDate] falls on a Monday — the weekly-streak reset day.
     */
    fun shouldResetWeeklyStreak(activityDate: LocalDate): Boolean =
        activityDate.dayOfWeek == DayOfWeek.MONDAY
}
