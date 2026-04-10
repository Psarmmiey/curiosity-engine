package com.curiosityengine.app.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String = "",
    val date: String = "",
    val topic: String = "",
    val category: String = "",
    val emoji: String = "",
    val contentJson: String = "",
    val quizJson: String = "",
    val referencesJson: String = "",
    val youtubeVideoId: String? = null,
    val isBonus: Boolean = false,
    val isRead: Boolean = false,
    val quizScore: Int? = null,
    val syncedAt: Long = 0L
)
