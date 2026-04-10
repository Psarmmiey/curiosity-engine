package com.curiosityengine.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String = "",

    @ColumnInfo(name = "chain_streak")
    val chainStreak: Int = 0,

    @ColumnInfo(name = "best_streak")
    val bestStreak: Int = 0,

    @ColumnInfo(name = "last_completed_date")
    val lastCompletedDate: String? = null,

    @ColumnInfo(name = "weekly_streak")
    val weeklyStreak: Int = 0,

    @ColumnInfo(name = "week_start_date")
    val weekStartDate: String? = null,

    /** Comma-separated list of ISO day-of-week integers (1=Mon … 7=Sun) */
    @ColumnInfo(name = "week_days_done")
    val weekDaysDone: String = "",

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L
)
