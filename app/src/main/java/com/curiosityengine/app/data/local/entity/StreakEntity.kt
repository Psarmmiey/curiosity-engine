package com.curiosityengine.app.data.local.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey val userId: String = "",
    val chainStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDate: String? = null,
    val weeklyStreak: Int = 0,
    val weekStartDate: String? = null
)
