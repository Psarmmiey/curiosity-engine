package com.curiosityengine.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val CuriosityShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small       = RoundedCornerShape(8.dp),   // Buttons
    medium      = RoundedCornerShape(12.dp),  // Chips, badges
    large       = RoundedCornerShape(16.dp),  // Cards
    extraLarge  = RoundedCornerShape(24.dp),  // Bottom sheets (top corners)
)
