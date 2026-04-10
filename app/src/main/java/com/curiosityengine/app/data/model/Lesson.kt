package com.curiosityengine.app.data.model

data class Lesson(
    val id: String,
    val date: String,
    val topic: String,
    val category: Category,
    val emoji: String,
    val blocks: List<LessonBlock>,
    val quiz: List<QuizQuestion>,
    val references: List<Reference>,
    val youtubeVideoId: String? = null,
    val isBonus: Boolean = false,
    val isRead: Boolean = false,
    val readAt: Long? = null,
    val quizScore: Int? = null,
    val userId: String? = null,
    val syncedAt: Long = 0L
)
