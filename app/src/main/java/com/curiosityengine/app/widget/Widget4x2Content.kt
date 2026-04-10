package com.curiosityengine.app.widget

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.curiosityengine.app.MainActivity
import androidx.compose.ui.graphics.Color
import androidx.glance.appwidget.cornerRadius
import androidx.glance.color.colorProviders

private val NavyBackground = Color(0xFF0F1E35)
private val NavySurface = Color(0xFF162844)
private val GoldColor = Color(0xFFC8932A)
private val GreenColor = Color(0xFF4AC58A)
private val WhiteText = Color(0xFFE8E4DB)
private val MutedText = Color(0xFF8899AA)

@Composable
fun Widget4x2Content(state: CuriosityWidgetState) {
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
            .background(NavyBackground)
            .cornerRadius(16)
            .clickable(tapAction),
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12),
        ) {
            // Top row: emoji + topic + right side streak/progress
            Row(
                modifier = GlanceModifier.fillMaxWidth().wrapContentHeight(),
                verticalAlignment = Alignment.Vertical.Top,
            ) {
                // Left side: emoji + topic + category
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                ) {
                    Text(
                        text = state.emoji,
                        style = TextStyle(fontSize = androidx.glance.unit.sp(28)),
                    )
                    Spacer(GlanceModifier.height(4))
                    Text(
                        text = state.topic,
                        style = TextStyle(
                            color = ColorProvider(WhiteText),
                            fontSize = androidx.glance.unit.sp(14),
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 2,
                    )
                    if (state.category.isNotBlank()) {
                        Spacer(GlanceModifier.height(2))
                        Text(
                            text = state.category,
                            style = TextStyle(
                                color = ColorProvider(MutedText),
                                fontSize = androidx.glance.unit.sp(11),
                            ),
                        )
                    }
                }

                Spacer(GlanceModifier.width(8))

                // Right side: streak + progress
                Column(
                    horizontalAlignment = Alignment.Horizontal.End,
                ) {
                    // Streak badge
                    Row(
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                    ) {
                        Text(
                            text = "\uD83D\uDD25",
                            style = TextStyle(fontSize = androidx.glance.unit.sp(18)),
                        )
                        Spacer(GlanceModifier.width(2))
                        Text(
                            text = "${state.chainStreak}",
                            style = TextStyle(
                                color = ColorProvider(GoldColor),
                                fontSize = androidx.glance.unit.sp(18),
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                    Spacer(GlanceModifier.height(4))
                    // Progress text
                    Text(
                        text = "${state.dailyProgressMinutes}/${state.dailyTargetMinutes} min",
                        style = TextStyle(
                            color = ColorProvider(MutedText),
                            fontSize = androidx.glance.unit.sp(11),
                        ),
                    )
                }
            }

            Spacer(GlanceModifier.defaultWeight())

            // Bottom row: read status dot
            Row(
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                val (dotColor, statusText) = when {
                    state.quizDone -> Pair(GreenColor, "\u2713\u2713 Quiz done")
                    state.isRead -> Pair(GreenColor, "\u2713 Read")
                    else -> Pair(GoldColor, "Tap to read")
                }
                Box(
                    modifier = GlanceModifier
                        .size(8)
                        .background(dotColor)
                        .cornerRadius(4),
                )
                Spacer(GlanceModifier.width(6))
                Text(
                    text = statusText,
                    style = TextStyle(
                        color = ColorProvider(dotColor),
                        fontSize = androidx.glance.unit.sp(11),
                    ),
                )
            }
        }
    }
}
