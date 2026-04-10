package com.curiosityengine.app.data.model

data class QuizAnswer(
    val questionId: String,
    val selectedIndex: Int,
    val isCorrect: Boolean,
    val timeTakenMs: Long = 0L
)

data class QuizResult(
    val lessonId: String,
    val userId: String,
    val answers: List<QuizAnswer>,
    val score: Int,
    val totalQuestions: Int,
    val completedAt: Long = System.currentTimeMillis()
) {
    val percentage: Float get() = if (totalQuestions > 0) score.toFloat() / totalQuestions else 0f
    val passed: Boolean get() = percentage >= 0.7f
}
