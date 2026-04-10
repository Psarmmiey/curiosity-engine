package com.curiosityengine.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.curiosityengine.app.ui.theme.BrandGold
import com.curiosityengine.app.ui.theme.LatoFamily
import com.curiosityengine.app.ui.theme.SurfaceElevated

private val BadgeShape = RoundedCornerShape(18.dp)

@Composable
fun StreakBadge(
    streak: Int,
    modifier: Modifier = Modifier,
) {
    val glowModifier = if (streak >= 7) {
        Modifier.shadow(
            elevation = 12.dp,
            shape = BadgeShape,
            ambientColor = BrandGold.copy(alpha = 0.6f),
            spotColor = BrandGold.copy(alpha = 0.8f),
        )
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .then(glowModifier)
            .clip(BadgeShape)
            .background(SurfaceElevated)
            .border(width = 1.5.dp, color = BrandGold, shape = BadgeShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "\uD83D\uDD25",  // 🔥
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "$streak",
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = LatoFamily,
                fontWeight = FontWeight.Bold,
            ),
            color = BrandGold,
        )
    }
}
