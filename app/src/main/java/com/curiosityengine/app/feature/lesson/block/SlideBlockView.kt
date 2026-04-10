package com.curiosityengine.app.feature.lesson.block

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.curiosityengine.app.data.model.LessonBlock
import com.curiosityengine.app.ui.theme.DmSerifDisplayFamily
import com.curiosityengine.app.ui.theme.LatoFamily
import com.curiosityengine.app.ui.theme.SurfaceCard
import com.curiosityengine.app.ui.theme.TextPrimary
import com.curiosityengine.app.ui.theme.categoryColor

@Composable
fun SlideBlockView(
    block: LessonBlock.Slide,
    category: String,
    modifier: Modifier = Modifier,
) {
    val accentColor = categoryColor(category)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .height(IntrinsicSize.Min),
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accentColor),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // Headline
            Text(
                text = block.title,
                style = TextStyle(
                    fontFamily = DmSerifDisplayFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    lineHeight = 28.sp,
                ),
                color = TextPrimary,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Body text as a paragraph
            Text(
                text = block.body,
                style = TextStyle(
                    fontFamily = LatoFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                ),
                color = TextPrimary,
            )

            // Optional image URL reference (text label only — full image handled by ImageBlockView)
            if (!block.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📎 ${block.imageUrl}",
                    style = TextStyle(
                        fontFamily = LatoFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                    ),
                    color = accentColor,
                )
            }
        }
    }
}
