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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.curiosityengine.app.ui.theme.SurfaceElevated
import com.curiosityengine.app.ui.theme.TextMuted
import com.curiosityengine.app.ui.theme.TextPrimary
import com.curiosityengine.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReviewScreen(
    userId: String = "",
    onComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: WeeklyReviewViewModel = hiltViewModel(),
) {
    LaunchedEffect(userId) {
        viewModel.loadWeeklyQuiz(userId)
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
            title = { Text("Exit Weekly Review?") },
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Weekly Review",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                        )
                        WeeklyChallengeBadge()
                    }
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
                            onClick = { viewModel.loadWeeklyQuiz(userId) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
                        ) {
                            Text("Retry", color = BackgroundDeep)
                        }
                    }
                }

                is QuizUiState.InProgress -> {
                    WeeklyQuizInProgressContent(
                        state = state,
                        onSelectAnswer = { optionIndex ->
                            viewModel.selectAnswer(state.currentIndex, optionIndex)
                        },
                        onNext = {
                            val isLast = state.currentIndex == state.questions.size - 1
                            if (isLast) {
                                viewModel.finishQuiz()
                            } else {
                                viewModel.nextQuestion()
                            }
                        },
                    )
                }

                is QuizUiState.Completed -> {
                    WeeklyResultsContent(
                        state = state,
                        onComplete = onComplete,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyChallengeBadge() {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = BrandGold.copy(alpha = 0.18f),
    ) {
        Text(
            text = "7-Day Challenge",
            style = MaterialTheme.typography.labelSmall,
            color = BrandGold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun WeeklyQuizInProgressContent(
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
        // Progress indicator with question count label
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Question ${state.currentIndex + 1} of ${state.questions.size}",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
            )
            Text(
                text = "${state.revealedAnswers.size} answered",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
            )
        }

        // Progress dots
        WeeklyProgressDotsRow(
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
            WeeklyOptionButton(
                text = option,
                optionIndex = idx,
                question = question,
                isRevealed = isRevealed,
                selectedOptionIndex = selectedOption,
                onClick = { onSelectAnswer(idx) },
            )
        }

        // Next / Finish button
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
private fun WeeklyProgressDotsRow(
    total: Int,
    currentIndex: Int,
    selectedAnswers: Map<Int, Int>,
    questions: List<QuizQuestion>,
    revealedAnswers: Set<Int>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until total) {
            val isCurrent = i == currentIndex
            val isAnswered = i in revealedAnswers
            val isCorrect = isAnswered && selectedAnswers[i] == questions[i].correctIndex

            val dotColor = when {
                isCurrent -> BrandGold
                isAnswered && isCorrect -> SuccessGreen
                isAnswered && !isCorrect -> ErrorRed
                else -> TextMuted
            }

            Box(
                modifier = Modifier
                    .size(if (isCurrent) 10.dp else 7.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }
    }
}

@Composable
private fun WeeklyOptionButton(
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
        label = "weekly_option_bg_$optionIndex",
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

@Composable
private fun WeeklyResultsContent(
    state: QuizUiState.Completed,
    onComplete: () -> Unit,
) {
    val correctByQuestion = state.questions.indices.map { idx ->
        val selected = state.selectedAnswers[idx]
        selected != null && selected == state.questions[idx].correctIndex
    }

    // Build a simple category breakdown: group by first word of question as a proxy
    // In a real impl, QuizQuestion would carry a category field
    val correctCount = correctByQuestion.count { it }
    val incorrectCount = correctByQuestion.count { !it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Weekly Review Complete!",
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
            textAlign = TextAlign.Center,
        )

        WeeklyScoreRing(score = state.score, total = state.totalQuestions)

        WeeklyStarRating(score = state.score, total = state.totalQuestions)

        HorizontalDivider(color = SurfaceElevated)

        // Accuracy breakdown
        Text(
            text = "Accuracy Breakdown",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth(),
        )

        AccuracyBreakdownCard(
            correctCount = correctCount,
            incorrectCount = incorrectCount,
            total = state.totalQuestions,
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Back to Home", style = MaterialTheme.typography.labelLarge, color = BackgroundDeep)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun WeeklyScoreRing(score: Int, total: Int) {
    val progress = if (total == 0) 0f else score.toFloat() / total.toFloat()
    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 900),
        label = "weekly_score_ring",
    )

    val ringColor = when {
        progress >= 1f -> SuccessGreen
        progress >= 0.6f -> BrandGold
        else -> ErrorRed
    }

    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(140.dp)) {
            val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 12.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
            drawArc(
                color = SurfaceElevated,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
            )
            if (animatedProgress > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = stroke,
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
            )
            Text(
                text = "of $total",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun WeeklyStarRating(score: Int, total: Int) {
    val stars = when {
        total == 0 -> 0
        score == total -> 3
        score.toFloat() / total >= 0.6f -> 2
        else -> 1
    }

    val label = when (stars) {
        3 -> "7-Day Streak Master!"
        2 -> "Great Week!"
        else -> "Keep Improving"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 1..3) {
                Text(
                    text = if (i <= stars) "★" else "☆",
                    style = MaterialTheme.typography.displaySmall,
                    color = if (i <= stars) BrandGold else TextMuted,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (stars == 3) SuccessGreen else TextSecondary,
        )
    }
}

@Composable
private fun AccuracyBreakdownCard(
    correctCount: Int,
    incorrectCount: Int,
    total: Int,
) {
    val accuracy = if (total == 0) 0f else correctCount.toFloat() / total * 100f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AccuracyStatItem(
                label = "Correct",
                value = correctCount.toString(),
                color = SuccessGreen,
            )
            AccuracyStatItem(
                label = "Incorrect",
                value = incorrectCount.toString(),
                color = ErrorRed,
            )
            AccuracyStatItem(
                label = "Accuracy",
                value = "${accuracy.toInt()}%",
                color = BrandGold,
            )
        }

        // Simple accuracy bar
        val animatedAccuracy by animateFloatAsState(
            targetValue = accuracy / 100f,
            animationSpec = tween(durationMillis = 900),
            label = "accuracy_bar",
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceElevated),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedAccuracy)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (accuracy >= 60f) SuccessGreen else ErrorRed),
            )
        }
    }
}

@Composable
private fun AccuracyStatItem(
    label: String,
    value: String,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}
