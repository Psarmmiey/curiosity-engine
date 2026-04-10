package com.curiosityengine.app.feature.share

import com.curiosityengine.app.data.model.Lesson
import com.curiosityengine.app.data.model.Streak

sealed class ShareCardType {
    data class LessonCard(val lesson: Lesson) : ShareCardType()
    data class QuizScoreCard(val lesson: Lesson, val score: Int, val total: Int) : ShareCardType()
    data class StreakMilestoneCard(val milestone: Int, val streak: Streak) : ShareCardType()
    data class WeeklyStatsCard(val lessonsCompleted: Int, val xpEarned: Int, val streak: Streak) : ShareCardType()
    data class QuoteCard(val quote: String, val attribution: String, val topic: String) : ShareCardType()
}
