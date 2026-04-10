package com.curiosityengine.app.feature.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.curiosityengine.app.data.model.QuizQuestion
import com.curiosityengine.app.ui.theme.BackgroundDeep
import com.curiosityengine.app.ui.theme.BrandGold
import com.curiosityengine.app.ui.theme.ErrorRed
import com.curiosityengine.app.ui.theme.SuccessGreen
import com.curiosityengine.app.ui.theme.SurfaceCard
import com.curiosityengine.app.ui.theme.TextMuted
import com.curiosityengine.app.ui.theme.TextPrimary
import com.curiosityengine.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    lessonId: String,
    userId: String = "",
    onQuizComplete: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    LaunchedEffect(lessonId) {
        viewModel.loadQuiz(lessonId, userId)
    }

    val uiState by viewModel.uiState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    val isInProgress = uiState is QuizUiState.InProgress

    BackHandler(enabled = isInProgress) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Quiz?") },
            text = { Text("Your progress will be lost if you leave now.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onNavigateBack()
                }) {
                    Text("Exit", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Keep Going", color = BrandGold)
                }
            },
            containerColor = SurfaceCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
        )
    }

    Scaffold(
        containerColor = BackgroundDeep,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quiz",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isInProgress) showExitDialog = true else onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDeep),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (val state = uiState) {
                is QuizUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = BrandGold,
                    )
                }

                is QuizUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadQuiz(lessonId, userId) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                        ) {
                            Text("Retry", color = BackgroundDeep)
                        }
                    }
                }

                is QuizUiState.InProgress -> {
                    QuizInProgressContent(
                        state = state,
                        onSelectAnswer = { optionIndex ->
                            viewModel.selectAnswer(state.currentIndex, optionIndex)
                        },
                        onNext = {
                            val isLast = state.currentIndex == state.questions.size - 1
                            if (isLast) {
                                viewModel.finishQuiz(userId, lessonId)
                                onQuizComplete(lessonId)
                            } else {
                                viewModel.nextQuestion()
                            }
                        },
                    )
                }

                is QuizUiState.Completed -> {
                    LaunchedEffect(Unit) {
                        onQuizComplete(lessonId)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizInProgressContent(
    state: QuizUiState.InProgress,
    onSelectAnswer: (Int) -> Unit,
    onNext: () -> Unit,
) {
    val question = state.questions[state.currentIndex]
    val isRevealed = state.currentIndex in state.revealedAnswers
    val selectedOption = state.selectedAnswers[state.currentIndex]
    val isLastQuestion = state.currentIndex == state.questions.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Progress dots
        ProgressDotsRow(
            total = state.questions.size,
            currentIndex = state.currentIndex,
            selectedAnswers = state.selectedAnswers,
            questions = state.questions,
            revealedAnswers = state.revealedAnswers,
        )

        // Question card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .padding(20.dp),
        ) {
            Text(
                text = question.question,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
            )
        }

        // Options
        question.options.forEachIndexed { idx, option ->
            OptionButton(
                text = option,
                optionIndex = idx,
                question = question,
                isRevealed = isRevealed,
                selectedOptionIndex = selectedOption,
                onClick = { onSelectAnswer(idx) },
            )
        }

        // Next / See Results button
        if (isRevealed) {
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = if (isLastQuestion) "See Results" else "Next Question",
                    style = MaterialTheme.typography.labelLarge,
                    color = BackgroundDeep,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ProgressDotsRow(
    total: Int,
    currentIndex: Int,
    selectedAnswers: Map<Int, Int>,
    questions: List<QuizQuestion>,
    revealedAnswers: Set<Int>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until total) {
            val isCurrent = i == currentIndex
            val isAnswered = i in revealedAnswers
            val isCorrect = isAnswered &&
                    selectedAnswers[i] == questions[i].correctIndex

            val dotColor = when {
                isCurrent -> BrandGold
                isAnswered && isCorrect -> SuccessGreen
                isAnswered && !isCorrect -> ErrorRed
                else -> TextMuted
            }

            Box(
                modifier = Modifier
                    .size(if (isCurrent) 12.dp else 9.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }
    }
}

@Composable
private fun OptionButton(
    text: String,
    optionIndex: Int,
    question: QuizQuestion,
    isRevealed: Boolean,
    selectedOptionIndex: Int?,
    onClick: () -> Unit,
) {
    val isCorrectOption = optionIndex == question.correctIndex
    val isSelectedOption = optionIndex == selectedOptionIndex
    val isWrongSelected = isRevealed && isSelectedOption && !isCorrectOption
    val shouldHighlightCorrect = isRevealed && isCorrectOption

    val targetBackground = when {
        shouldHighlightCorrect -> SuccessGreen
        isWrongSelected -> ErrorRed
        else -> Color.Transparent
    }

    val animatedBackground by animateColorAsState(
        targetValue = targetBackground,
        animationSpec = tween(durationMillis = 300),
        label = "option_bg_$optionIndex",
    )

    val borderColor = when {
        shouldHighlightCorrect -> SuccessGreen
        isWrongSelected -> ErrorRed
        isSelectedOption -> BrandGold
        else -> TextMuted
    }

    val textColor = when {
        shouldHighlightCorrect || isWrongSelected -> Color.White
        else -> TextPrimary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(animatedBackground)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(enabled = !isRevealed, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )
    }
}
