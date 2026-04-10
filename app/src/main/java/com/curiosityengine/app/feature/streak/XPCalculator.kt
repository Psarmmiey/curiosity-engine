package com.curiosityengine.app.feature.streak

object XPCalculator {

    const val XP_PER_LESSON = 50
    const val XP_QUIZ_BONUS_PERFECT = 30  // 5/5
    const val XP_QUIZ_BONUS_GOOD = 15     // 4/5
    const val XP_QUIZ_BONUS_PASS = 5      // 3/5

    /**
     * Returns the XP multiplier for a given chain streak length.
     *
     * Tiers:
     *  0–6   → 1.0×
     *  7–13  → 1.5×
     *  14–29 → 2.0×
     *  30+   → 3.0×
     */
    fun getStreakMultiplier(chainStreak: Int): Float = when {
        chainStreak >= 30 -> 3.0f
        chainStreak >= 14 -> 2.0f
        chainStreak >= 7  -> 1.5f
        else              -> 1.0f
    }

    /**
     * XP awarded for completing a lesson, factoring in the streak multiplier.
     * Result is rounded down to the nearest integer.
     */
    fun calculateLessonXp(chainStreak: Int): Int =
        (XP_PER_LESSON * getStreakMultiplier(chainStreak)).toInt()

    /**
     * Bonus XP based on quiz performance.
     *
     * Thresholds (out of [totalQuestions]):
     *  100%  → [XP_QUIZ_BONUS_PERFECT]
     *  ≥ 80% → [XP_QUIZ_BONUS_GOOD]
     *  ≥ 60% → [XP_QUIZ_BONUS_PASS]
     *  < 60% → 0
     *
     * When [totalQuestions] is 0 or negative, returns 0.
     */
    fun calculateQuizBonus(score: Int, totalQuestions: Int): Int {
        if (totalQuestions <= 0) return 0
        val ratio = score.toFloat() / totalQuestions.toFloat()
        return when {
            ratio >= 1.0f  -> XP_QUIZ_BONUS_PERFECT
            ratio >= 0.80f -> XP_QUIZ_BONUS_GOOD
            ratio >= 0.60f -> XP_QUIZ_BONUS_PASS
            else           -> 0
        }
    }

    /**
     * Total XP for a lesson completion.
     *
     * = [calculateLessonXp] + optional quiz bonus.
     *
     * @param quizScore      nullable — pass null when there was no quiz
     * @param totalQuestions total number of quiz questions (used only when [quizScore] != null)
     */
    fun calculateTotalXp(chainStreak: Int, quizScore: Int?, totalQuestions: Int): Int {
        val lessonXp = calculateLessonXp(chainStreak)
        val quizBonus = if (quizScore != null) calculateQuizBonus(quizScore, totalQuestions) else 0
        return lessonXp + quizBonus
    }
}
