package com.curiosityengine.app.feature.lesson.block

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.curiosityengine.app.data.model.LessonBlock
import com.curiosityengine.app.ui.theme.DmSerifDisplayFamily
import com.curiosityengine.app.ui.theme.LatoFamily
import com.curiosityengine.app.ui.theme.SurfaceElevated
import com.curiosityengine.app.ui.theme.TextPrimary
import com.curiosityengine.app.ui.theme.TextSecondary
import com.curiosityengine.app.ui.theme.categoryColor

@Composable
fun FlashcardBlockView(
    block: LessonBlock.Flashcard,
    category: String,
    modifier: Modifier = Modifier,
) {
    val cards = block.items
    if (cards.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { cards.size })
    // Track flip state per card
    val flippedStates = remember(cards.size) {
        mutableStateListOf(*Array(cards.size) { false })
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp),
        ) { page ->
            val isFlipped = flippedStates[page]
            val rotation by animateFloatAsState(
                targetValue = if (isFlipped) 180f else 0f,
                animationSpec = tween(durationMillis = 400),
                label = "card_flip_$page",
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceElevated)
                    .clickable { flippedStates[page] = !flippedStates[page] }
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (rotation <= 90f) {
                    // Front face
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp),
                    ) {
                        Text(
                            text = cards[page].front,
                            style = TextStyle(
                                fontFamily = DmSerifDisplayFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 18.sp,
                                lineHeight = 26.sp,
                                textAlign = TextAlign.Center,
                            ),
                            color = TextPrimary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to flip",
                            style = TextStyle(
                                fontFamily = LatoFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                            ),
                            color = TextSecondary,
                        )
                    }
                } else {
                    // Back face — counter-rotate so text reads normally
                    Box(
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = cards[page].back,
                            style = TextStyle(
                                fontFamily = LatoFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Center,
                            ),
                            color = TextPrimary,
                            modifier = Modifier.padding(24.dp),
                        )
                    }
                }
            }
        }

        // Page indicator dots
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(cards.size) { index ->
                val isSelected = pagerState.currentPage == index
                val dotColor = if (isSelected) categoryColor(category) else TextSecondary.copy(alpha = 0.4f)
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                )
            }
        }
    }
}
