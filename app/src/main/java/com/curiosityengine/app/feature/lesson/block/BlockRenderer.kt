package com.curiosityengine.app.feature.lesson.block

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.curiosityengine.app.data.model.LessonBlock

@Composable
fun BlockRenderer(
    block: LessonBlock,
    category: String,
    modifier: Modifier = Modifier,
) {
    when (block) {
        is LessonBlock.Text      -> TextBlockView(block, modifier)
        is LessonBlock.Image     -> ImageBlockView(block, modifier)
        is LessonBlock.Flashcard -> FlashcardBlockView(block, category, modifier)
        is LessonBlock.Slide     -> SlideBlockView(block, category, modifier)
        is LessonBlock.Quote     -> QuoteBlockView(block, modifier)
        is LessonBlock.Video     -> VideoBlockView(block, modifier)
    }
}
