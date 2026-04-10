package com.curiosityengine.app.streak

import com.curiosityengine.app.feature.streak.XPCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class XPCalculatorTest {

    // -----------------------------------------------------------------------
    // getStreakMultiplier
    // -----------------------------------------------------------------------

    @Test
    fun `multiplier is 1_0 for streak 0`() {
        assertEquals(1.0f, XPCalculator.getStreakMultiplier(0))
    }

    @Test
    fun `multiplier is 1_0 for streak 6`() {
        assertEquals(1.0f, XPCalculator.getStreakMultiplier(6))
    }

    @Test
    fun `multiplier is 1_5 for streak 7`() {
        assertEquals(1.5f, XPCalculator.getStreakMultiplier(7))
    }

    @Test
    fun `multiplier is 1_5 for streak 13`() {
        assertEquals(1.5f, XPCalculator.getStreakMultiplier(13))
    }

    @Test
    fun `multiplier is 2_0 for streak 14`() {
        assertEquals(2.0f, XPCalculator.getStreakMultiplier(14))
    }

    @Test
    fun `multiplier is 2_0 for streak 29`() {
        assertEquals(2.0f, XPCalculator.getStreakMultiplier(29))
    }

    @Test
    fun `multiplier is 3_0 for streak 30`() {
        assertEquals(3.0f, XPCalculator.getStreakMultiplier(30))
    }

    @Test
    fun `multiplier is 3_0 for streak 100`() {
        assertEquals(3.0f, XPCalculator.getStreakMultiplier(100))
    }

    // -----------------------------------------------------------------------
    // calculateLessonXp
    // -----------------------------------------------------------------------

    @Test
    fun `lesson XP at streak 0 is 50`() {
        assertEquals(50, XPCalculator.calculateLessonXp(0))
    }

    @Test
    fun `lesson XP at streak 7 is 75`() {
        // 50 * 1.5 = 75
        assertEquals(75, XPCalculator.calculateLessonXp(7))
    }

    @Test
    fun `lesson XP at streak 14 is 100`() {
        // 50 * 2.0 = 100
        assertEquals(100, XPCalculator.calculateLessonXp(14))
    }

    @Test
    fun `lesson XP at streak 30 is 150`() {
        // 50 * 3.0 = 150
        assertEquals(150, XPCalculator.calculateLessonXp(30))
    }

    // -----------------------------------------------------------------------
    // calculateQuizBonus
    // -----------------------------------------------------------------------

    @Test
    fun `quiz bonus for 5 out of 5 is perfect bonus`() {
        assertEquals(XPCalculator.XP_QUIZ_BONUS_PERFECT, XPCalculator.calculateQuizBonus(5, 5))
    }

    @Test
    fun `quiz bonus for 4 out of 5 is good bonus`() {
        assertEquals(XPCalculator.XP_QUIZ_BONUS_GOOD, XPCalculator.calculateQuizBonus(4, 5))
    }

    @Test
    fun `quiz bonus for 3 out of 5 is pass bonus`() {
        assertEquals(XPCalculator.XP_QUIZ_BONUS_PASS, XPCalculator.calculateQuizBonus(3, 5))
    }

    @Test
    fun `quiz bonus for 2 out of 5 is zero`() {
        assertEquals(0, XPCalculator.calculateQuizBonus(2, 5))
    }

    @Test
    fun `quiz bonus for 0 out of 5 is zero`() {
        assertEquals(0, XPCalculator.calculateQuizBonus(0, 5))
    }

    @Test
    fun `quiz bonus when totalQuestions is zero returns zero`() {
        assertEquals(0, XPCalculator.calculateQuizBonus(5, 0))
    }

    @Test
    fun `quiz bonus for 8 out of 10 is good bonus`() {
        // 80% → XP_QUIZ_BONUS_GOOD
        assertEquals(XPCalculator.XP_QUIZ_BONUS_GOOD, XPCalculator.calculateQuizBonus(8, 10))
    }

    @Test
    fun `quiz bonus for 6 out of 10 is pass bonus`() {
        // 60% → XP_QUIZ_BONUS_PASS
        assertEquals(XPCalculator.XP_QUIZ_BONUS_PASS, XPCalculator.calculateQuizBonus(6, 10))
    }

    @Test
    fun `quiz bonus for 10 out of 10 is perfect bonus`() {
        assertEquals(XPCalculator.XP_QUIZ_BONUS_PERFECT, XPCalculator.calculateQuizBonus(10, 10))
    }

    // -----------------------------------------------------------------------
    // calculateTotalXp
    // -----------------------------------------------------------------------

    @Test
    fun `total XP with no quiz and streak 0`() {
        // 50 + 0 = 50
        assertEquals(50, XPCalculator.calculateTotalXp(0, null, 5))
    }

    @Test
    fun `total XP with perfect quiz and streak 0`() {
        // 50 + 30 = 80
        assertEquals(80, XPCalculator.calculateTotalXp(0, 5, 5))
    }

    @Test
    fun `total XP with perfect quiz and streak 7`() {
        // (50 * 1.5) + 30 = 75 + 30 = 105
        assertEquals(105, XPCalculator.calculateTotalXp(7, 5, 5))
    }

    @Test
    fun `total XP with good quiz and streak 14`() {
        // (50 * 2.0) + 15 = 100 + 15 = 115
        assertEquals(115, XPCalculator.calculateTotalXp(14, 4, 5))
    }

    @Test
    fun `total XP with no quiz and streak 30`() {
        // (50 * 3.0) + 0 = 150
        assertEquals(150, XPCalculator.calculateTotalXp(30, null, 0))
    }

    @Test
    fun `total XP with failed quiz and streak 30`() {
        // (50 * 3.0) + 0 = 150 (no bonus for failing quiz)
        assertEquals(150, XPCalculator.calculateTotalXp(30, 1, 5))
    }
}
