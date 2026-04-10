package com.curiosityengine.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FlashcardItem(
    val front: String,
    val back: String
)

@Serializable
sealed class LessonBlock {

    @Serializable
    @SerialName("text")
    data class Text(
        val markdown: String
    ) : LessonBlock()

    @Serializable
    @SerialName("image")
    data class Image(
        val url: String,
        val caption: String? = null,
        val altText: String? = null
    ) : LessonBlock()

    @Serializable
    @SerialName("flashcard")
    data class Flashcard(
        val items: List<FlashcardItem>
    ) : LessonBlock()

    @Serializable
    @SerialName("slide")
    data class Slide(
        val title: String,
        val body: String,
        val imageUrl: String? = null
    ) : LessonBlock()

    @Serializable
    @SerialName("quote")
    data class Quote(
        val text: String,
        val attribution: String? = null
    ) : LessonBlock()

    @Serializable
    @SerialName("video")
    data class Video(
        val youtubeVideoId: String,
        val title: String? = null,
        val startSeconds: Int = 0
    ) : LessonBlock()
}
