package com.curiosityengine.app.feature.quiz

import com.curiosityengine.app.data.model.QuizQuestion

sealed class QuizUiState {
    data object Loading : QuizUiState()

    data class InProgress(
        val questions: List<QuizQuestion>,
        val currentIndex: Int = 0,
        val selectedAnswers: Map<Int, Int> = emptyMap(), // questionIndex -> selectedOptionIndex
        val revealedAnswers: Set<Int> = emptySet()       // questions where answer has been revealed
    ) : QuizUiState()

    data class Completed(
        val questions: List<QuizQuestion>,
        val selectedAnswers: Map<Int, Int>,
        val score: Int,
        val totalQuestions: Int
    ) : QuizUiState()

    data class Error(val message: String) : QuizUiState()
}
