package com.curiosityengine.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_prefs")
data class UserPrefsEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String = "",

    @ColumnInfo(name = "display_name")
    val displayName: String? = null,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,

    /** Comma-separated list of category names */
    @ColumnInfo(name = "preferred_categories")
    val preferredCategories: String = "",

    @ColumnInfo(name = "daily_target")
    val dailyTarget: Int = 1,

    @ColumnInfo(name = "reminder_time")
    val reminderTime: String = "08:00",

    @ColumnInfo(name = "email_daily_nudge")
    val emailDailyNudge: Boolean = false,

    @ColumnInfo(name = "email_weekly_digest")
    val emailWeeklyDigest: Boolean = true,

    @ColumnInfo(name = "lesson_model")
    val lessonModel: String = "gemini-2.5-pro",

    @ColumnInfo(name = "images_enabled")
    val imagesEnabled: Boolean = true,

    @ColumnInfo(name = "video_enabled")
    val videoEnabled: Boolean = true
)
