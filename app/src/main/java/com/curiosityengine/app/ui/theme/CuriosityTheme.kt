package com.curiosityengine.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary          = BrandGold,
    onPrimary        = BackgroundDeep,
    primaryContainer = SurfaceCard,
    onPrimaryContainer = TextPrimary,
    secondary        = Amber,
    onSecondary      = BackgroundDeep,
    background       = BackgroundDeep,
    onBackground     = TextPrimary,
    surface          = SurfaceCard,
    onSurface        = TextPrimary,
    surfaceVariant   = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error            = ErrorRed,
    onError          = TextPrimary,
    outline          = TextMuted,
)

@Composable
fun CuriosityTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = CuriosityTypography,
        shapes      = CuriosityShapes,
        content     = content,
    )
}
