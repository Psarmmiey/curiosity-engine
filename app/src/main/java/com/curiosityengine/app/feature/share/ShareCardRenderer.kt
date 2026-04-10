package com.curiosityengine.app.feature.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.curiosityengine.app.R
import com.curiosityengine.app.data.model.LessonBlock
import com.curiosityengine.app.ui.theme.categoryColor
import com.curiosityengine.app.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ShareCardRenderer {

    // ── Colour constants (ARGB ints) ──────────────────────────────────────
    private const val COLOR_BACKGROUND_DEEP = 0xFF0F1E35.toInt()
    private const val COLOR_BACKGROUND_BOTTOM = 0xFF162844.toInt()
    private const val COLOR_BRAND_GOLD = 0xFFC8932A.toInt()
    private const val COLOR_TEXT_PRIMARY = 0xFFE8E4DB.toInt()
    private const val COLOR_TEXT_SECONDARY = 0xFF8899AA.toInt()
    private const val COLOR_TEXT_MUTED = 0xFF4A5568.toInt()
    private const val COLOR_WHITE = 0xFFFFFFFF.toInt()

    private const val SIZE = 1080 // pixels (square)
    private const val PADDING = 80f

    suspend fun render(context: Context, cardType: ShareCardType): Bitmap =
        withContext(Dispatchers.Default) {
            val bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val dmSerif = loadTypeface(context, R.font.dm_serif_display_regular, Typeface.SERIF)
            val lato = loadTypeface(context, R.font.lato_regular, Typeface.SANS_SERIF)

            when (cardType) {
                is ShareCardType.LessonCard ->
                    drawLessonCard(canvas, cardType, dmSerif, lato)

                is ShareCardType.QuizScoreCard ->
                    drawQuizScoreCard(canvas, cardType, dmSerif, lato)

                is ShareCardType.StreakMilestoneCard ->
                    drawStreakMilestoneCard(canvas, cardType, dmSerif, lato)

                is ShareCardType.WeeklyStatsCard ->
                    drawWeeklyStatsCard(canvas, cardType, dmSerif, lato)

                is ShareCardType.QuoteCard ->
                    drawQuoteCard(canvas, cardType, dmSerif, lato)
            }

            bitmap
        }

    // ── Font helpers ──────────────────────────────────────────────────────

    private fun loadTypeface(context: Context, fontRes: Int, fallback: Typeface): Typeface {
        return try {
            val base = ResourcesCompat.getFont(context, fontRes) ?: fallback
            Typeface.create(base, Typeface.NORMAL)
        } catch (e: Exception) {
            Typeface.create(fallback, Typeface.NORMAL)
        }
    }

    // ── Background helpers ────────────────────────────────────────────────

    private fun drawNavyGradient(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(
            0f, 0f, 0f, SIZE.toFloat(),
            COLOR_BACKGROUND_DEEP, COLOR_BACKGROUND_BOTTOM,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, SIZE.toFloat(), SIZE.toFloat(), paint)
    }

    private fun drawWatermark(canvas: Canvas, lato: Typeface) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = lato
            textSize = 20f * (SIZE / 360f)
            color = COLOR_TEXT_MUTED
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "curiosityengine.app",
            SIZE / 2f,
            SIZE - PADDING / 2f,
            paint
        )
    }

    // ── Text helpers ──────────────────────────────────────────────────────

    /**
     * Draws wrapped text and returns the Y coordinate just below the last drawn line.
     */
    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        paint: Paint,
        x: Float,
        startY: Float,
        maxWidth: Float
    ): Float {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()

        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = StringBuilder(candidate)
            } else {
                if (current.isNotEmpty()) lines.add(current.toString())
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())

        val lineHeight = paint.textSize * 1.3f
        var y = startY
        for (line in lines) {
            canvas.drawText(line, x, y, paint)
            y += lineHeight
        }
        return y
    }

    // ── Category colour helper ────────────────────────────────────────────

    private fun categoryColorInt(category: String): Int {
        val composeColor = categoryColor(category)
        return Color.argb(
            (composeColor.alpha * 255).toInt(),
            (composeColor.red * 255).toInt(),
            (composeColor.green * 255).toInt(),
            (composeColor.blue * 255).toInt()
        )
    }

    // ─────────────────────────────────────────────────────────────────────
    // 1. LESSON CARD
    // ─────────────────────────────────────────────────────────────────────

    private fun drawLessonCard(
        canvas: Canvas,
        card: ShareCardType.LessonCard,
        dmSerif: Typeface,
        lato: Typeface
    ) {
        val lesson = card.lesson
        val scale = SIZE / 360f  // scale factor relative to a 360-unit design grid

        drawNavyGradient(canvas)

        // Left accent bar
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = categoryColorInt(lesson.category.displayName)
        }
        canvas.drawRect(0f, 0f, 16f * scale, SIZE.toFloat(), accentPaint)

        val contentLeft = PADDING + 16f * scale
        val contentWidth = SIZE - contentLeft - PADDING

        // Emoji
        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = lato
            textSize = 80f * scale
            color = COLOR_WHITE
            textAlign = Paint.Align.LEFT
        }
        var curY = PADDING + emojiPaint.textSize
        canvas.drawText(lesson.emoji, contentLeft, curY, emojiPaint)
        curY += emojiPaint.textSize * 0.4f

        // Topic title
        val topicPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = dmSerif
            textSize = 48f * scale
            color = COLOR_WHITE
            textAlign = Paint.Align.LEFT
        }
        curY += topicPaint.textSize * 0.8f
        curY = drawWrappedText(canvas, lesson.topic, topicPaint, contentLeft, curY, contentWidth)
        curY += topicPaint.textSize * 0.6f

        // Key insight
        val insight = lesson.blocks
            .filterIsInstance<LessonBlock.Text>()
            .firstOrNull()
            ?.markdown
            ?.take(200)
            ?: ""

        if (insight.isNotEmpty()) {
            val insightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = lato
                textSize = 28f * scale
                color = COLOR_TEXT_PRIMARY
                textAlign = Paint.Align.LEFT
            }
            curY = drawWrappedText(canvas, insight, insightPaint, contentLeft, curY, contentWidth)
        }

        // Category name chip
        val chipText = lesson.category.displayName
        val chipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = lato
            textSize = 20f * scale
            color = COLOR_BRAND_GOLD
            textAlign = Paint.Align.LEFT
        }
        val chipY = SIZE - PADDING * 1.5f
        canvas.drawText(chipText, contentLeft, chipY, chipPaint)

        drawWatermark(canvas, lato)
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2. QUIZ SCORE CARD
    // ─────────────────────────────────────────────────────────────────────

    private fun drawQuizScoreCard(
        canvas: Canvas,
        card: ShareCardType.QuizScoreCard,
        dmSerif: Typeface,
        lato: Typeface
    ) {
        val scale = SIZE / 360f

        drawNavyGradient(canvas)

        // Gold circular badge top-right
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_BRAND_GOLD
        }
        val badgeRadius = 60f * scale
        val badgeCx = SIZE - PADDING - badgeRadius
        val badgeCy = PADDING + badgeRadius
        canvas.drawCircle(badgeCx, badgeCy, badgeRadius, badgePaint)

        val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = dmSerif
            textSize = 24f * scale
            color = COLOR_BACKGROUND_DEEP
            textAlign = Paint.Align.CENTER
        }
        val textBounds = android.graphics.Rect()
        badgeTextPaint.getTextBounds("QUIZ", 0, 4, textBounds)
        canvas.drawText("QUIZ", badgeCx, badgeCy + textBounds.height() / 2f, badgeTextPaint)

        // Large score text centred
        val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = dmSerif
            textSize = 120f * scale
            color = COLOR_BRAND_GOLD
            textAlign = Paint.Align.CENTER
        }
        val scoreText = "${card.score}/${card.total}"
        canvas.drawText(scoreText, SIZE / 2f, SIZE / 2f - 60f * scale, scorePaint)

        // Star row
        val totalStars = 5
        val filledStars = when {
            card.total <= 0 -> 0
            else -> Math.round(card.score.toFloat() / card.total * totalStars)
        }
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = lato
            textSize = 60f * scale
            textAlign = Paint.Align.CENTER
        }
        val starSpacing = 70f * scale
        val starRowStartX = SIZE / 2f - (totalStars - 1) / 2f * starSpacing
        val starY = SIZE / 2f + 20f * scale
        for (i in 0 until totalStars) {
            starPaint.color = if (i < filledStars) COLOR_BRAND_GOLD else COLOR_TEXT_MUTED
            canvas.drawText("★", starRowStartX + i * starSpacing, starY, starPaint)
        }

        // Topic name
        val topicPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = dmSerif
            textSize = 32f * scale
            color = COLOR_WHITE
            textAlign = Paint.Align.CENTER
        }
        val topicY = starY + 80f * scale
        drawWrappedText(
            canvas, card.lesson.topic, topicPaint,
            SIZE / 2f, topicY,
            SIZE - PADDING * 2
        )

        drawWatermark(canvas, lato)
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3. STREAK MILESTONE CARD
    // ─────────────────────────────────────────────────────────────────────

    private fun drawStreakMilestoneCard(
        canvas: Canvas,
        card: ShareCardType.StreakMilestoneCard,
        dmSerif: Typeface,
        lato: Typeface
    ) {
        val scale = SIZE / 360f

        // Radial gradient background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val goldCenter = Color.argb(51, 200, 147, 42) // BrandGold at ~20% opacity
        bgPaint.shader = RadialGradient(
            SIZE / 2f, SIZE / 2f, SIZE * 0.7f,
            goldCenter, COLOR_BACKGROUND_DEEP,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, SIZE.toFloat(), SIZE.toFloat(), bgPaint)

        // Giant fire emoji
        val firePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = lato
            textSize = 200f * scale
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🔥", SIZE / 2f, SIZE / 2f - 100f * scale, firePaint)

        // Milestone number
        val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = dmSerif
            textSize = 160f * scale
            color = COLOR_BRAND_GOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            card.milestone.toString(),
            SIZE / 2f,
            SIZE / 2f + 120f * scale,
            numberPaint
        )

        // "Day Streak" label
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = lato
            textSize = 48f * scale
            color = COLOR_WHITE
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Day Streak", SIZE / 2f, SIZE / 2f + 200f * scale, labelPaint)

        drawWatermark(canvas, lato)
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4. WEEKLY STATS CARD
    // ─────────────────────────────────────────────────────────────────────

    private fun drawWeeklyStatsCard(
        canvas: Canvas,
        card: ShareCardType.WeeklyStatsCard,
        dmSerif: Typeface,
        lato: Typeface
    ) {
        val scale = SIZE / 360f

        drawNavyGradient(canvas)

        // "This Week" heading
        val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = dmSerif
            textSize = 40f * scale
            color = COLOR_BRAND_GOLD
            textAlign = Paint.Align.LEFT
        }
        var curY = PADDING + headingPaint.textSize
        canvas.drawText("This Week", PADDING, curY, headingPaint)
        curY += headingPaint.textSize * 1.5f

        // Stat rows
        val statPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = lato
            textSize = 52f * scale
            color = COLOR_TEXT_PRIMARY
            textAlign = Paint.Align.LEFT
        }
        val lineHeight = statPaint.textSize * 1.6f

        val stats = listOf(
            "📚 ${card.lessonsCompleted} lessons",
            "⚡ ${card.xpEarned} XP",
            "🔥 ${card.streak.chainStreak}-day streak"
        )

        for (stat in stats) {
            canvas.drawText(stat, PADDING, curY, statPaint)
            curY += lineHeight
        }

        drawWatermark(canvas, lato)
    }

    // ─────────────────────────────────────────────────────────────────────
    // 5. QUOTE CARD
    // ─────────────────────────────────────────────────────────────────────

    private fun drawQuoteCard(
        canvas: Canvas,
        card: ShareCardType.QuoteCard,
        dmSerif: Typeface,
        lato: Typeface
    ) {
        val scale = SIZE / 360f

        // Deep navy background (solid)
        canvas.drawColor(COLOR_BACKGROUND_DEEP)

        // Gold left border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_BRAND_GOLD
        }
        canvas.drawRect(0f, 0f, 24f * scale, SIZE.toFloat(), borderPaint)

        val contentLeft = 24f * scale + PADDING
        val contentWidth = SIZE - contentLeft - PADDING

        // Quote text in italics
        val quotePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(dmSerif, Typeface.ITALIC)
            textSize = 44f * scale
            color = COLOR_WHITE
            textAlign = Paint.Align.LEFT
        }

        var curY = PADDING * 2 + quotePaint.textSize
        curY = drawWrappedText(canvas, card.quote, quotePaint, contentLeft, curY, contentWidth)
        curY += quotePaint.textSize * 0.8f

        // Attribution
        val attrPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = lato
            textSize = 32f * scale
            color = COLOR_TEXT_SECONDARY
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("— ${card.attribution}", contentLeft, curY, attrPaint)
        curY += attrPaint.textSize * 1.5f

        // Topic chip
        val chipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = lato
            textSize = 20f * scale
            color = COLOR_BRAND_GOLD
            textAlign = Paint.Align.LEFT
        }
        val chipY = SIZE - PADDING * 1.5f
        canvas.drawText(card.topic, contentLeft, chipY, chipPaint)

        drawWatermark(canvas, lato)
    }
}
