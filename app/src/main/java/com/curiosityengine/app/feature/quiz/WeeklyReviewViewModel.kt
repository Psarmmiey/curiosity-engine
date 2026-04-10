package com.curiosityengine.app.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curiosityengine.app.data.model.QuizAnswer
import com.curiosityengine.app.data.model.QuizResult
import com.curiosityengine.app.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val WEEKLY_REVIEW_LESSON_ID = "weekly_review"

@HiltViewModel
class WeeklyReviewViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var autoAdvanceJob: Job? = null
    private var currentUserId: String = ""

    fun loadWeeklyQuiz(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            try {
                val questions = quizRepository.getWeeklyQuiz(userId)
                if (questions.isEmpty()) {
                    _uiState.value = QuizUiState.Error("No weekly quiz available at this time.")
                } else {
                    _uiState.value = QuizUiState.InProgress(questions = questions)
                }
            } catch (e: Exception) {
                Timber.e(e, "WeeklyReviewViewModel: failed to load weekly quiz for user $userId")
                _uiState.value =
                    QuizUiState.Error(e.message ?: "Failed to load weekly quiz. Please try again.")
            }
        }
    }

    fun selectAnswer(questionIndex: Int, optionIndex: Int) {
        val current = _uiState.value as? QuizUiState.InProgress ?: return
        if (questionIndex in current.revealedAnswers) return

        val updatedAnswers = current.selectedAnswers + (questionIndex to optionIndex)
        val updatedRevealed = current.revealedAnswers + questionIndex

        _uiState.value = current.copy(
            selectedAnswers = updatedAnswers,
            revealedAnswers = updatedRevealed,
        )

        autoAdvanceJob?.cancel()
        autoAdvanceJob = viewModelScope.launch {
            delay(1_500L)
            nextQuestion()
        }
    }

    fun nextQuestion() {
        autoAdvanceJob?.cancel()
        val current = _uiState.value as? QuizUiState.InProgress ?: return
        val nextIndex = current.currentIndex + 1
        if (nextIndex < current.questions.size) {
            _uiState.value = current.copy(currentIndex = nextIndex)
        }
    }

    fun finishQuiz() {
        val current = _uiState.value as? QuizUiState.InProgress ?: return
        autoAdvanceJob?.cancel()

        val score = current.questions.indices.count { idx ->
            val selected = current.selectedAnswers[idx]
            selected != null && selected == current.questions[idx].correctIndex
        }

        val answers = current.questions.mapIndexed { idx, question ->
            val selected = current.selectedAnswers[idx] ?: -1
            QuizAnswer(
                questionId = question.id,
                selectedIndex = selected,
                isCorrect = selected == question.correctIndex,
            )
        }

        _uiState.value = QuizUiState.Completed(
            questions = current.questions,
            selectedAnswers = current.selectedAnswers,
            score = score,
            totalQuestions = current.questions.size,
        )

        viewModelScope.launch {
            try {
                quizRepository.submitQuizResult(
                    QuizResult(
                        lessonId = WEEKLY_REVIEW_LESSON_ID,
                        userId = currentUserId,
                        answers = answers,
                        score = score,
                        totalQuestions = current.questions.size,
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "WeeklyReviewViewModel: failed to submit weekly quiz result")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoAdvanceJob?.cancel()
    }
}
