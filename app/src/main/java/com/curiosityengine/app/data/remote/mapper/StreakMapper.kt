package com.curiosityengine.app.data.remote.mapper

import com.curiosityengine.app.data.local.entity.StreakEntity
import com.curiosityengine.app.data.model.Streak
import com.curiosityengine.app.data.remote.dto.StreakDto

fun StreakDto.toEntity(): StreakEntity = StreakEntity(
    userId = userId,
    chainStreak = chainStreak,
    bestStreak = bestStreak,
    lastCompletedDate = lastCompletedDate,
    weeklyStreak = weeklyStreak,
    weekStartDate = weekStartDate,
    weekDaysDone = weekDaysDone.joinToString(","),
    updatedAt = updatedAt
)

fun StreakEntity.toDomain(): Streak = Streak(
    userId = userId,
    chainStreak = chainStreak,
    bestStreak = bestStreak,
    lastCompletedDate = lastCompletedDate,
    weeklyStreak = weeklyStreak,
    weekStartDate = weekStartDate,
    weekDaysDone = if (weekDaysDone.isBlank()) emptyList()
    else weekDaysDone.split(",").mapNotNull { it.trim().toIntOrNull() },
    updatedAt = updatedAt
)

fun Streak.toEntity(): StreakEntity = StreakEntity(
    userId = userId,
    chainStreak = chainStreak,
    bestStreak = bestStreak,
    lastCompletedDate = lastCompletedDate,
    weeklyStreak = weeklyStreak,
    weekStartDate = weekStartDate,
    weekDaysDone = weekDaysDone.joinToString(","),
    updatedAt = updatedAt
)
