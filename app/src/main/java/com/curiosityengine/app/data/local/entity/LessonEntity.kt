package com.curiosityengine.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = "",

    @ColumnInfo(name = "date")
    val date: String = "",

    @ColumnInfo(name = "topic")
    val topic: String = "",

    @ColumnInfo(name = "category")
    val category: String = "",

    @ColumnInfo(name = "emoji")
    val emoji: String = "",

    @ColumnInfo(name = "content_json")
    val contentJson: String = "",

    @ColumnInfo(name = "quiz_json")
    val quizJson: String = "",

    @ColumnInfo(name = "references_json")
    val referencesJson: String = "",

    @ColumnInfo(name = "youtube_video_id")
    val youtubeVideoId: String? = null,

    @ColumnInfo(name = "is_bonus")
    val isBonus: Boolean = false,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "read_at")
    val readAt: Long? = null,

    @ColumnInfo(name = "quiz_score")
    val quizScore: Int? = null,

    @ColumnInfo(name = "user_id")
    val userId: String? = null,

    @ColumnInfo(name = "synced_at")
    val syncedAt: Long = 0L
)
