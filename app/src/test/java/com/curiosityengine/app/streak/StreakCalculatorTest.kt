package com.curiosityengine.app.streak

import com.curiosityengine.app.data.model.Streak
import com.curiosityengine.app.feature.streak.StreakCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun date(isoString: String): LocalDate = LocalDate.parse(isoString)

    private fun streak(
        userId: String = "user1",
        chainStreak: Int = 0,
        bestStreak: Int = 0,
        lastCompletedDate: String? = null,
        weeklyStreak: Int = 0,
        weekStartDate: String? = null,
        weekDaysDone: List<Int> = emptyList(),
    ) = Streak(
        userId = userId,
        chainStreak = chainStreak,
        bestStreak = bestStreak,
        lastCompletedDate = lastCompletedDate,
        weeklyStreak = weeklyStreak,
        weekStartDate = weekStartDate,
        weekDaysDone = weekDaysDone,
    )

    // -----------------------------------------------------------------------
    // computeChainStreak
    // -----------------------------------------------------------------------

    @Test
    fun `new user - null lastActivityDate - chain streak becomes 1`() {
        val current = streak()
        val result = StreakCalculator.computeChainStreak(current, date("2026-04-06"))
        assertEquals(1, result.chainStreak)
    }

    @Test
    fun `consecutive day - chain streak increments`() {
        val current = streak(chainStreak = 5, bestStreak = 5, lastCompletedDate = "2026-04-05")
        val result = StreakCalculator.computeChainStreak(current, date("2026-04-06"))
        assertEquals(6, result.chainStreak)
    }

    @Test
    fun `same day - chain streak does not change`() {
        val current = streak(chainStreak = 3, bestStreak = 3, lastCompletedDate = "2026-04-06")
        val result = StreakCalculator.computeChainStreak(current, date("2026-04-06"))
        assertEquals(3, result.chainStreak)
    }

    @Test
    fun `gap of 2 days - chain streak resets to 1`() {
        val current = streak(chainStreak = 10, bestStreak = 10, lastCompletedDate = "2026-04-04")
        val result = StreakCalculator.computeChainStreak(current, date("2026-04-06"))
        assertEquals(1, result.chainStreak)
    }

    @Test
    fun `gap of 7 days - chain streak resets to 1`() {
        val current = streak(chainStreak = 20, bestStreak = 20, lastCompletedDate = "2026-03-30")
        val result = StreakCalculator.computeChainStreak(current, date("2026-04-06"))
        assertEquals(1, result.chainStreak)
    }

    @Test
    fun `bestStreak never decreases after streak break`() {
        val current = streak(chainStreak = 15, bestStreak = 15, lastCompletedDate = "2026-03-01")
        val result = StreakCalculator.computeChainStreak(current, date("2026-04-06"))
        assertEquals(1, result.chainStreak)
        assertEquals(15, result.bestStreak)  // preserved
    }

    @Test
    fun `bestStreak updates when new chain exceeds it`() {
        val current = streak(chainStreak = 4, bestStreak = 4, lastCompletedDate = "2026-04-05")
        val result = StreakCalculator.computeChainStreak(current, date("2026-04-06"))
        assertEquals(5, result.chainStreak)
        assertEquals(5, result.bestStreak)
    }

    @Test
    fun `bestStreak stays the same when chain is lower`() {
        val current = streak(chainStreak = 3, bestStreak = 10, lastCompletedDate = "2026-04-05")
        val result = StreakCalculator.computeChainStreak(current, date("2026-04-06"))
        assertEquals(4, result.chainStreak)
        assertEquals(10, result.bestStreak)  // still 10
    }

    // -----------------------------------------------------------------------
    // computeWeeklyStreak
    // -----------------------------------------------------------------------

    @Test
    fun `weekly streak counts distinct days in current week`() {
        // Monday 2026-04-06 is the start of the test week
        val mondayStr = "2026-04-06"
        val current = streak(weekStartDate = mondayStr, weekDaysDone = listOf(1))  // Mon already done
        // Add Tuesday (dow=2)
        val result = StreakCalculator.computeWeeklyStreak(current, date("2026-04-07"))
        assertEquals(2, result.weeklyStreak)
        assertTrue(result.weekDaysDone.containsAll(listOf(1, 2)))
    }

    @Test
    fun `weekly streak does not double-count same day`() {
        val mondayStr = "2026-04-06"
        val current = streak(weekStartDate = mondayStr, weekDaysDone = listOf(1, 2), weeklyStreak = 2)
        // Tuesday again
        val result = StreakCalculator.computeWeeklyStreak(current, date("2026-04-07"))
        assertEquals(2, result.weeklyStreak)
    }

    @Test
    fun `new week resets weekDaysDone`() {
        // Pretend saved data is from the previous week
        val lastWeekStr = "2026-03-30"
        val current = streak(weekStartDate = lastWeekStr, weekDaysDone = listOf(1, 2, 3), weeklyStreak = 3)
        // Activity on Monday of new week
        val result = StreakCalculator.computeWeeklyStreak(current, date("2026-04-06"))
        assertEquals(1, result.weeklyStreak)
    }

    // -----------------------------------------------------------------------
    // isStreakBroken
    // -----------------------------------------------------------------------

    @Test
    fun `isStreakBroken - null lastActivityDate returns false`() {
        assertFalse(StreakCalculator.isStreakBroken(null, date("2026-04-06")))
    }

    @Test
    fun `isStreakBroken - consecutive days returns false`() {
        assertFalse(StreakCalculator.isStreakBroken(date("2026-04-05"), date("2026-04-06")))
    }

    @Test
    fun `isStreakBroken - same day returns false`() {
        assertFalse(StreakCalculator.isStreakBroken(date("2026-04-06"), date("2026-04-06")))
    }

    @Test
    fun `isStreakBroken - gap of 2 days returns true`() {
        assertTrue(StreakCalculator.isStreakBroken(date("2026-04-04"), date("2026-04-06")))
    }

    @Test
    fun `isStreakBroken - gap of many days returns true`() {
        assertTrue(StreakCalculator.isStreakBroken(date("2026-01-01"), date("2026-04-06")))
    }

    // -----------------------------------------------------------------------
    // shouldResetWeeklyStreak
    // -----------------------------------------------------------------------

    @Test
    fun `shouldResetWeeklyStreak returns true on Monday`() {
        // 2026-04-06 is a Monday
        assertTrue(StreakCalculator.shouldResetWeeklyStreak(date("2026-04-06")))
    }

    @Test
    fun `shouldResetWeeklyStreak returns false on Tuesday`() {
        // 2026-04-07 is a Tuesday
        assertFalse(StreakCalculator.shouldResetWeeklyStreak(date("2026-04-07")))
    }

    @Test
    fun `shouldResetWeeklyStreak returns false on Sunday`() {
        // 2026-04-12 is a Sunday
        assertFalse(StreakCalculator.shouldResetWeeklyStreak(date("2026-04-12")))
    }

    @Test
    fun `shouldResetWeeklyStreak returns false on Wednesday`() {
        assertFalse(StreakCalculator.shouldResetWeeklyStreak(date("2026-04-08")))
    }
}
