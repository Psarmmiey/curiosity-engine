package com.curiosityengine.app.feature.lesson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.curiosityengine.app.data.model.Lesson
import com.curiosityengine.app.data.model.Streak
import com.curiosityengine.app.data.repository.StreakRepository
import com.curiosityengine.app.ui.components.CuriosityPrimaryButton
import com.curiosityengine.app.ui.components.CuriositySecondaryButton
import com.curiosityengine.app.ui.theme.BrandGold
import com.curiosityengine.app.ui.theme.DmSerifDisplayFamily
import com.curiosityengine.app.ui.theme.TextPrimary
import com.curiosityengine.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonCompletionOverlay(
    lesson: Lesson,
    streakRepository: StreakRepository,
    onNavigateToQuiz: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val streak by produceState<Streak?>(initialValue = null) {
        value = lesson.userId?.let { uid ->
            runCatching { streakRepository.getStreakOnce(uid) }.getOrNull()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Lesson Complete! \uD83C\uDF89",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = DmSerifDisplayFamily,
                    fontWeight = FontWeight.Normal,
                ),
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )

            Text(
                text = lesson.topic,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )

            // Streak display
            streak?.let { s ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "\uD83D\uDD25 ${s.chainStreak}",
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = DmSerifDisplayFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 40.sp,
                        ),
                        color = BrandGold,
                    )
                    Text(
                        text = if (s.chainStreak == 1) "day streak" else "day streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    if (s.bestStreak > 0 && s.chainStreak == s.bestStreak) {
                        Text(
                            text = "\u2B50 New personal best!",
                            style = MaterialTheme.typography.labelMedium,
                            color = BrandGold,
                        )
                    }
                }
            } ?: run {
                // Streak not available yet — show a generic celebration message
                Text(
                    text = "Keep up the great work!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CTA buttons
            if (lesson.quiz.isNotEmpty()) {
                CuriosityPrimaryButton(
                    text = "Take Quiz",
                    onClick = {
                        onDismiss()
                        onNavigateToQuiz(lesson.id)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            CuriositySecondaryButton(
                text = "Share Lesson",
                onClick = { /* Share functionality — implemented by share module */ },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
