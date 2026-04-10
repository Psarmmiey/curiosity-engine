package com.curiosityengine.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.curiosityengine.app.data.model.Lesson
import com.curiosityengine.app.data.model.Streak
import com.curiosityengine.app.data.model.UserPrefs
import com.curiosityengine.app.ui.components.CategoryChip
import com.curiosityengine.app.ui.components.CuriosityCard
import com.curiosityengine.app.ui.components.ProgressRing
import com.curiosityengine.app.ui.components.ShimmerBox
import com.curiosityengine.app.ui.components.StreakBadge
import com.curiosityengine.app.ui.theme.BackgroundDeep
import com.curiosityengine.app.ui.theme.BrandGold
import com.curiosityengine.app.ui.theme.DmSerifDisplayFamily
import com.curiosityengine.app.ui.theme.LatoFamily
import com.curiosityengine.app.ui.theme.SuccessGreen
import com.curiosityengine.app.ui.theme.SurfaceCard
import com.curiosityengine.app.ui.theme.SurfaceElevated
import com.curiosityengine.app.ui.theme.TextPrimary
import com.curiosityengine.app.ui.theme.TextSecondary
import java.time.LocalTime

@Composable
fun HomeScreen(
    onNavigateToLesson: (String) -> Unit,
    onNavigateToDoomScroll: () -> Unit,
    onNavigateToWeeklyReview: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDeep,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToDoomScroll,
                containerColor = BrandGold,
                contentColor = Color(0xFF0F1E35),
            ) {
                Text(
                    text = "\u26A1 Quick Learns",
                    fontFamily = LatoFamily,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // Greeting row
            item {
                GreetingRow(userPrefs = uiState.userPrefs)
            }

            // Streak row
            item {
                StreakRow(streak = uiState.streak)
            }

            // Daily progress
            item {
                DailyProgressRow(
                    progressMinutes = uiState.dailyProgressMinutes,
                    targetMinutes = uiState.userPrefs?.dailyTarget?.times(10) ?: 10,
                )
            }

            // Today's lesson card
            item {
                TodayLessonCard(
                    lesson = uiState.todayLesson,
                    isLoading = uiState.isLoading,
                    onReadNow = { onNavigateToLesson("today") },
                )
            }

            // Weekly Review card (if weeklyStreak >= 3)
            val weeklyStreak = uiState.streak?.weeklyStreak ?: 0
            if (weeklyStreak >= 3) {
                item {
                    WeeklyReviewCard(onNavigateToWeeklyReview = onNavigateToWeeklyReview)
                }
            }

            // Continue Learning header
            if (uiState.recentLessons.isNotEmpty()) {
                item {
                    Text(
                        text = "Continue Learning",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                items(uiState.recentLessons) { lesson ->
                    RecentLessonRow(
                        lesson = lesson,
                        onClick = { onNavigateToLesson(lesson.id) },
                    )
                }
            }

            // Bottom spacer for FAB
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun GreetingRow(userPrefs: UserPrefs?) {
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
    val name = userPrefs?.displayName ?: "Curious Mind"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "$greeting,\n$name!",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = DmSerifDisplayFamily,
                fontSize = 24.sp,
            ),
            color = TextPrimary,
            modifier = Modifier.weight(1f),
        )

        if (!userPrefs?.avatarUrl.isNullOrBlank()) {
            Spacer(Modifier.width(12.dp))
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(userPrefs!!.avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, BrandGold, CircleShape),
            )
        }
    }
}

@Composable
private fun StreakRow(streak: Streak?) {
    val chainStreak = streak?.chainStreak ?: 0
    val weekDaysDone = streak?.weekDaysDone ?: emptyList()

    CuriosityCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StreakBadge(streak = chainStreak)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "\uD83D\uDD25 $chainStreak-day streak",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                )
            }

            // Weekly dots: 7 circles, filled = done
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (dayIndex in 1..7) {
                    val isDone = weekDaysDone.contains(dayIndex)
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isDone) BrandGold else SurfaceElevated)
                            .border(1.dp, if (isDone) BrandGold else TextSecondary.copy(alpha = 0.3f), CircleShape),
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyProgressRow(progressMinutes: Int, targetMinutes: Int) {
    val progress = if (targetMinutes > 0) progressMinutes.toFloat() / targetMinutes.toFloat() else 0f

    CuriosityCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProgressRing(
                progress = progress,
                size = 72.dp,
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = "Daily Progress",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$progressMinutes / $targetMinutes min today",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun TodayLessonCard(
    lesson: Lesson?,
    isLoading: Boolean,
    onReadNow: () -> Unit,
) {
    CuriosityCard {
        when {
            isLoading && lesson == null -> {
                // Shimmer loading placeholder
                Column {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp)),
                    )
                    Spacer(Modifier.height(8.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp)),
                    )
                    Spacer(Modifier.height(12.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .width(120.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }
            }

            lesson == null -> {
                // Generating placeholder
                Column {
                    Text(
                        text = "\uD83E\uDDE0 Today's lesson is being prepared...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(8.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp)),
                    )
                }
            }

            else -> {
                // Lesson available
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = lesson.emoji,
                            fontSize = 36.sp,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Today's Lesson",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandGold,
                            )
                            Text(
                                text = lesson.topic,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CategoryChip(category = lesson.category.displayName)

                        // Estimated read time chip
                        val readMinutes = lesson.blocks.size * 2
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "~$readMinutes min",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                            )
                        }

                        if (lesson.isRead) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SuccessGreen.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "\u2713 Read",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    if (lesson.isRead) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                                    .align(Alignment.CenterVertically),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "\u2713",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                )
                            }
                            OutlinedButton(
                                onClick = onReadNow,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = TextSecondary,
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TextSecondary.copy(alpha = 0.4f)),
                            ) {
                                Text(
                                    text = "Read Again",
                                    fontFamily = LatoFamily,
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onReadNow,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandGold,
                                contentColor = Color(0xFF0F1E35),
                            ),
                        ) {
                            Text(
                                text = "Read Now",
                                fontFamily = LatoFamily,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentLessonRow(lesson: Lesson, onClick: () -> Unit) {
    CuriosityCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = lesson.emoji,
                fontSize = 24.sp,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.topic,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                CategoryChip(category = lesson.category.displayName)
            }
            Spacer(Modifier.width(8.dp))
            if (lesson.isRead) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "\u2713",
                        color = Color.White,
                        fontSize = 12.sp,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .border(2.dp, BrandGold, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun WeeklyReviewCard(onNavigateToWeeklyReview: () -> Unit) {
    CuriosityCard(onClick = onNavigateToWeeklyReview) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "\uD83D\uDCCA",
                fontSize = 28.sp,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Weekly Challenge Available",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )
                Text(
                    text = "Test your knowledge from this week",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }
            Text(
                text = "\u203A",
                style = MaterialTheme.typography.headlineMedium,
                color = BrandGold,
            )
        }
    }
}
