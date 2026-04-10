package com.curiosityengine.app.feature.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun QuizResultsScreen(
    lessonId: String,
    onNavigateHome: () -> Unit,
    onShareResult: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = BackgroundDeep) { innerPadding ->
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

                is QuizUiState.Completed -> {
                    ResultsContent(
                        state = state,
                        onNavigateHome = onNavigateHome,
                        onShareResult = onShareResult,
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
                    }
                }

                else -> {
                    // InProgress — shouldn't happen on results screen, show nothing
                }
            }
        }
    }
}

@Composable
private fun ResultsContent(
    state: QuizUiState.Completed,
    onNavigateHome: () -> Unit,
    onShareResult: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = "Quiz Complete!",
            style = MaterialTheme.typography.displaySmall,
            color = TextPrimary,
        )

        // Score ring
        ScoreRing(score = state.score, total = state.totalQuestions)

        // Star rating
        StarRating(score = state.score, total = state.totalQuestions)

        HorizontalDivider(color = SurfaceElevated)

        // Expandable explanations
        Text(
            text = "Review Answers",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth(),
        )

        state.questions.forEachIndexed { idx, question ->
            val selectedAnswer = state.selectedAnswers[idx]
            ExplanationCard(
                index = idx,
                question = question,
                selectedAnswerIndex = selectedAnswer,
            )
        }

        Spacer(Modifier.height(8.dp))

        // CTAs
        Button(
            onClick = onShareResult,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandGold),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Share Result", style = MaterialTheme.typography.labelLarge, color = BackgroundDeep)
        }

        OutlinedButton(
            onClick = onNavigateHome,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandGold),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text("Back to Home", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ScoreRing(score: Int, total: Int) {
    val progress = if (total == 0) 0f else score.toFloat() / total.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 900),
        label = "score_ring",
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
        Canvas(modifier = Modifier.size(140.dp)) {
            val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
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
private fun StarRating(score: Int, total: Int) {
    val stars = when {
        total == 0 -> 0
        score == total -> 3
        score.toFloat() / total >= 0.6f -> 2
        else -> 1
    }

    val label = when (stars) {
        3 -> "Perfect Score!"
        2 -> "Great Work!"
        else -> "Keep Practising"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 1..3) {
                Text(
                    text = if (i <= stars) "★" else "☆",
                    fontSize = 36.sp,
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
private fun ExplanationCard(
    index: Int,
    question: QuizQuestion,
    selectedAnswerIndex: Int?,
) {
    var expanded by remember { mutableStateOf(false) }
    val isCorrect = selectedAnswerIndex == question.correctIndex

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = if (isCorrect) "✓" else "✗",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCorrect) SuccessGreen else ErrorRed,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Q${index + 1}: ${question.question}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = TextSecondary,
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HorizontalDivider(color = SurfaceElevated)
                Text(
                    text = "Correct Answer: ${question.options[question.correctIndex]}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SuccessGreen,
                )
                if (selectedAnswerIndex != null && selectedAnswerIndex != question.correctIndex) {
                    Text(
                        text = "Your Answer: ${question.options[selectedAnswerIndex]}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ErrorRed,
                    )
                }
                if (!question.explanation.isNullOrBlank()) {
                    Text(
                        text = question.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}
