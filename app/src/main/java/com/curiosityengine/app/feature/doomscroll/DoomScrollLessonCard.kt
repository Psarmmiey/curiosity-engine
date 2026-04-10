package com.curiosityengine.app.feature.doomscroll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.curiosityengine.app.data.model.Lesson
import com.curiosityengine.app.data.model.LessonBlock
import com.curiosityengine.app.feature.lesson.block.BlockRenderer
import com.curiosityengine.app.ui.components.CategoryChip
import com.curiosityengine.app.ui.theme.BackgroundDeep
import com.curiosityengine.app.ui.theme.BrandGold
import com.curiosityengine.app.ui.theme.TextPrimary
import com.curiosityengine.app.ui.theme.TextSecondary
import com.curiosityengine.app.ui.theme.categoryColor

@Composable
fun DoomScrollLessonCard(
    lesson: Lesson,
    onOpenFullLesson: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = categoryColor(lesson.category.displayName)

    // Only surface Text and Image blocks in the feed card
    val displayBlocks = lesson.blocks.filter { block ->
        block is LessonBlock.Text || block is LessonBlock.Image
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeep),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Category colour left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxSize()
                    .background(accentColor),
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Header: emoji + topic title
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                ) {
                    Text(
                        text = lesson.emoji,
                        style = MaterialTheme.typography.displayMedium,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = lesson.topic,
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary,
                    )
                }

                // Scrollable content area — fills remaining space above the bottom bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 80.dp), // leave room below gradient
                    ) {
                        displayBlocks.forEach { block ->
                            BlockRenderer(
                                block = block,
                                category = lesson.category.displayName,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Gradient overlay at bottom to hint at more content
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        BackgroundDeep,
                                    ),
                                ),
                            ),
                    )
                }

                // Bottom row: category chip + "Read Full Article" button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundDeep)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    CategoryChip(category = lesson.category.displayName)

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { onOpenFullLesson(lesson.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGold,
                            contentColor = BackgroundDeep,
                        ),
                    ) {
                        Text(
                            text = "Read Full Article \u2192",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}
