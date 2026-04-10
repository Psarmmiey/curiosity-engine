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

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var autoAdvanceJob: Job? = null

    fun loadQuiz(lessonId: String, userId: String = "") {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            try {
                val questions = quizRepository.getQuizForLesson(lessonId, userId)
                if (questions.isEmpty()) {
                    _uiState.value = QuizUiState.Error("No questions available for this lesson.")
                } else {
                    _uiState.value = QuizUiState.InProgress(questions = questions)
                }
            } catch (e: Exception) {
                Timber.e(e, "QuizViewModel: failed to load quiz for lesson $lessonId")
                _uiState.value = QuizUiState.Error(e.message ?: "Failed to load quiz. Please try again.")
            }
        }
    }

    fun selectAnswer(questionIndex: Int, optionIndex: Int) {
        val current = _uiState.value as? QuizUiState.InProgress ?: return
        // Do not allow re-selection after answer revealed
        if (questionIndex in current.revealedAnswers) return

        val updatedAnswers = current.selectedAnswers + (questionIndex to optionIndex)
        val updatedRevealed = current.revealedAnswers + questionIndex

        _uiState.value = current.copy(
            selectedAnswers = updatedAnswers,
            revealedAnswers = updatedRevealed,
        )

        // Auto-advance after 1.5 s if not on the last question
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
        // If on last question, caller handles navigation to results
    }

    fun finishQuiz(userId: String, lessonId: String) {
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
                        lessonId = lessonId,
                        userId = userId,
                        answers = answers,
                        score = score,
                        totalQuestions = current.questions.size,
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "QuizViewModel: failed to submit quiz result")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoAdvanceJob?.cancel()
    }
}
