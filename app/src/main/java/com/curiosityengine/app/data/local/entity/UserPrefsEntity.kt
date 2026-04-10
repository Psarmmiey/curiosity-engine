package com.curiosityengine.app.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_prefs")
data class UserPrefsEntity(
    @PrimaryKey val userId: String = "",
    val preferredCategories: String = "",
    val dailyTarget: Int = 1,
    val reminderTime: String = "08:00",
    val emailWeeklyDigest: Boolean = true,
    val lessonModel: String = "gemini-2.5-pro",
    val imagesEnabled: Boolean = true,
    val videoEnabled: Boolean = true
)
