package com.curiosityengine.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LessonDto(
    val id: String,
    val date: String,
    val topic: String,
    val category: String,
    val emoji: String,
    @SerialName("content_json") val contentJson: String,
    @SerialName("quiz_json") val quizJson: String,
    @SerialName("references_json") val referencesJson: String,
    @SerialName("youtube_video_id") val youtubeVideoId: String? = null,
    @SerialName("is_bonus") val isBonus: Boolean = false,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("synced_at") val syncedAt: Long = 0L
)
