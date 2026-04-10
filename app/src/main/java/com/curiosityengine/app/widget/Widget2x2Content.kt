package com.curiosityengine.app.widget

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color

private val NavyBackground2x2 = Color(0xFF0F1E35)
private val GoldColor2x2 = Color(0xFFC8932A)
private val GreenColor2x2 = Color(0xFF4AC58A)
private val WhiteText2x2 = Color(0xFFE8E4DB)
private val MutedText2x2 = Color(0xFF8899AA)

@Composable
fun Widget2x2Content(state: CuriosityWidgetState) {
    val deepLinkIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("curiosityengine://lesson/today"),
    ).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val tapAction = actionStartActivity(deepLinkIntent)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(NavyBackground2x2)
            .cornerRadius(16)
            .clickable(tapAction),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment = Alignment.Vertical.CenterVertically,
        ) {
            // Large streak number with fire emoji
            Row(
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Text(
                    text = "\uD83D\uDD25",
                    style = TextStyle(fontSize = androidx.glance.unit.sp(24)),
                )
                Spacer(GlanceModifier.width(4))
                Text(
                    text = "${state.chainStreak}",
                    style = TextStyle(
                        color = ColorProvider(GoldColor2x2),
                        fontSize = androidx.glance.unit.sp(36),
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }

            Spacer(GlanceModifier.height(8))

            // Read status dot
            val (dotColor, statusLabel) = when {
                state.quizDone -> Pair(GreenColor2x2, "\u2713\u2713")
                state.isRead -> Pair(GreenColor2x2, "\u2713")
                else -> Pair(GoldColor2x2, "\u25CF")
            }

            Row(
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(10)
                        .background(dotColor)
                        .cornerRadius(5),
                )
                Spacer(GlanceModifier.width(4))
                Text(
                    text = if (state.isRead || state.quizDone) "Done" else "Today",
                    style = TextStyle(
                        color = ColorProvider(dotColor),
                        fontSize = androidx.glance.unit.sp(12),
                    ),
                )
            }
        }
    }
}
