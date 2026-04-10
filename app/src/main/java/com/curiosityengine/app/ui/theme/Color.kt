package com.curiosityengine.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Core Palette ─────────────────────────────────────────────
val BackgroundDeep     = Color(0xFF0F1E35)
val SurfaceCard        = Color(0xFF162844)
val SurfaceElevated    = Color(0xFF1D3355)
val SurfaceModal       = Color(0xFF253F60)
val SurfacePressed     = Color(0xFF2A4A72)

val BrandGold          = Color(0xFFC8932A)
val Amber              = Color(0xFFE8A020)

val TextPrimary        = Color(0xFFE8E4DB)
val TextSecondary      = Color(0xFF8899AA)
val TextMuted          = Color(0xFF4A5568)

val ErrorRed           = Color(0xFFE05252)
val SuccessGreen       = Color(0xFF4AC58A)

// ── Category Colours ─────────────────────────────────────────
val CategoryScience    = Color(0xFF1A7A6E)
val CategoryNature     = Color(0xFF2D6B2A)
val CategoryHistory    = Color(0xFF8B5A1C)
val CategoryPsychology = Color(0xFF6B2A8B)
val CategoryTechnology = Color(0xFF1A4A8B)
val CategoryArtCulture = Color(0xFF8B1A4A)
val CategoryEconomics  = Color(0xFF2A6B4A)
val CategoryPhilosophy = Color(0xFF4A4A8B)
val CategorySurprise   = Color(0xFF8B4A1A)

fun categoryColor(category: String): Color = when (category.lowercase()) {
    "science"       -> CategoryScience
    "nature"        -> CategoryNature
    "history"       -> CategoryHistory
    "psychology"    -> CategoryPsychology
    "technology"    -> CategoryTechnology
    "art & culture" -> CategoryArtCulture
    "economics"     -> CategoryEconomics
    "philosophy"    -> CategoryPhilosophy
    else            -> CategorySurprise
}
