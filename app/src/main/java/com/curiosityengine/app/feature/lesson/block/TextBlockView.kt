package com.curiosityengine.app.feature.lesson.block

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.curiosityengine.app.data.model.LessonBlock
import com.curiosityengine.app.ui.theme.LatoFamily
import com.curiosityengine.app.ui.theme.TextPrimary

@Composable
fun TextBlockView(
    block: LessonBlock.Text,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = block.markdown,
            style = TextStyle(
                fontFamily = LatoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 28.sp,
            ),
            color = TextPrimary,
        )
    }
}
