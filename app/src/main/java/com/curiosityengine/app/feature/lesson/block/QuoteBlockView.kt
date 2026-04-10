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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.curiosityengine.app.data.model.LessonBlock
import com.curiosityengine.app.ui.theme.BrandGold
import com.curiosityengine.app.ui.theme.DmSerifDisplayFamily
import com.curiosityengine.app.ui.theme.LatoFamily
import com.curiosityengine.app.ui.theme.TextPrimary
import com.curiosityengine.app.ui.theme.TextSecondary

@Composable
fun QuoteBlockView(
    block: LessonBlock.Quote,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(IntrinsicSize.Min),
    ) {
        // 4dp gold left border
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(BrandGold),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
        ) {
            Text(
                text = block.text,
                style = TextStyle(
                    fontFamily = DmSerifDisplayFamily,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic,
                    fontSize = 22.sp,
                    lineHeight = 30.sp,
                ),
                color = TextPrimary,
            )

            if (!block.attribution.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\u2014 ${block.attribution}",
                    style = TextStyle(
                        fontFamily = LatoFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    ),
                    color = TextSecondary,
                )
            }
        }
    }
}
