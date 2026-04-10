package com.curiosityengine.app.feature.journal

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.curiosityengine.app.data.model.Lesson
import com.curiosityengine.app.ui.components.CategoryChip
import com.curiosityengine.app.ui.components.CuriosityCard
import com.curiosityengine.app.ui.theme.BackgroundDeep
import com.curiosityengine.app.ui.theme.BrandGold
import com.curiosityengine.app.ui.theme.LatoFamily
import com.curiosityengine.app.ui.theme.SuccessGreen
import com.curiosityengine.app.ui.theme.SurfaceCard
import com.curiosityengine.app.ui.theme.SurfaceElevated
import com.curiosityengine.app.ui.theme.TextPrimary
import com.curiosityengine.app.ui.theme.TextSecondary
import com.curiosityengine.app.util.DateTimeUtil
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onNavigateToLesson: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: JournalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDeep,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Learning Journal",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDeep,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item {
                WeekStripCalendar(
                    weekStart = uiState.selectedWeekStart,
                    lessonsThisWeek = uiState.lessonsThisWeek,
                    allLessons = uiState.allLessons,
                    onPrevWeek = { viewModel.prevWeek() },
                    onNextWeek = { viewModel.nextWeek() },
                )
            }

            item { Spacer(Modifier.height(4.dp)) }

            if (uiState.lessonsThisWeek.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyWeekState(onStartLearning = onNavigateBack)
                }
            } else {
                items(uiState.lessonsThisWeek, key = { it.id }) { lesson ->
                    LessonRow(
                        lesson = lesson,
                        onClick = { onNavigateToLesson(lesson.id) },
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun WeekStripCalendar(
    weekStart: LocalDate,
    lessonsThisWeek: List<Lesson>,
    allLessons: List<Lesson>,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    val today = DateTimeUtil.today()
    val currentWeekStart = DateTimeUtil.currentWeekStart()
    val isCurrentWeek = weekStart == currentWeekStart

    // Build set of dates that have completed lessons
    val completedDates = allLessons
        .filter { it.isRead }
        .mapNotNullTo(mutableSetOf()) {
            try { DateTimeUtil.parseDate(it.date) } catch (e: Exception) { null }
        }

    CuriosityCard {
        Column {
            // Month/year label + navigation row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onPrevWeek) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous week",
                        tint = BrandGold,
                    )
                }

                val weekEnd = weekStart.plusDays(6)
                val monthLabel = if (weekStart.month == weekEnd.month) {
                    weekStart.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                            " ${weekStart.year}"
                } else {
                    weekStart.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) +
                            " – " +
                            weekEnd.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) +
                            " ${weekEnd.year}"
                }
                Text(
                    text = monthLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                )

                IconButton(
                    onClick = onNextWeek,
                    enabled = !isCurrentWeek,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next week",
                        tint = if (isCurrentWeek) TextSecondary.copy(alpha = 0.3f) else BrandGold,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // 7-day strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (dayOffset in 0L..6L) {
                    val day = weekStart.plusDays(dayOffset)
                    val isToday = day == today
                    val hasCompletedLesson = completedDates.contains(day)
                    DayColumn(
                        date = day,
                        isToday = isToday,
                        hasCompletedLesson = hasCompletedLesson,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayColumn(
    date: LocalDate,
    isToday: Boolean,
    hasCompletedLesson: Boolean,
) {
    val dayLetter = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
    val dayNumber = date.dayOfMonth.toString()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = dayLetter,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isToday) BrandGold else Color.Transparent)
                .then(
                    if (!isToday) Modifier.border(
                        width = 1.dp,
                        color = SurfaceElevated,
                        shape = CircleShape,
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = dayNumber,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                ),
                color = if (isToday) Color(0xFF0F1E35) else TextPrimary,
                textAlign = TextAlign.Center,
            )
        }

        // Green dot indicator for completed lessons
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (hasCompletedLesson) SuccessGreen else Color.Transparent),
        )
    }
}

@Composable
private fun LessonRow(
    lesson: Lesson,
    onClick: () -> Unit,
) {
    CuriosityCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = lesson.emoji,
                fontSize = 28.sp,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.topic,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CategoryChip(category = lesson.category.displayName)
                    val readMinutes = lesson.blocks.size * 2
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "~$readMinutes min",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            // Quiz score badge (if taken)
            val score = lesson.quizScore
            if (score != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (score >= 4) SuccessGreen.copy(alpha = 0.15f)
                            else BrandGold.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "$score/5",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = if (score >= 4) SuccessGreen else BrandGold,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyWeekState(onStartLearning: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "📖",
            fontSize = 48.sp,
        )
        Text(
            text = "No lessons this week — start learning!",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onStartLearning,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandGold,
                contentColor = Color(0xFF0F1E35),
            ),
        ) {
            Text(
                text = "Start Learning",
                fontFamily = LatoFamily,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
