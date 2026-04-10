package com.curiosityengine.app.data.remote.mapper

import com.curiosityengine.app.data.local.entity.UserPrefsEntity
import com.curiosityengine.app.data.model.UserPrefs
import com.curiosityengine.app.data.remote.dto.UserDto

fun UserDto.toEntity(): UserPrefsEntity = UserPrefsEntity(
    userId = userId,
    displayName = displayName,
    email = email,
    avatarUrl = avatarUrl,
    preferredCategories = preferredCategories.joinToString(","),
    dailyTarget = dailyTarget,
    reminderTime = reminderTime,
    emailDailyNudge = emailDailyNudge,
    emailWeeklyDigest = emailWeeklyDigest,
    lessonModel = lessonModel,
    imagesEnabled = imagesEnabled,
    videoEnabled = videoEnabled
)

fun UserPrefsEntity.toDomain(): UserPrefs = UserPrefs(
    userId = userId,
    displayName = displayName,
    email = email,
    avatarUrl = avatarUrl,
    preferredCategories = if (preferredCategories.isBlank()) emptyList()
    else preferredCategories.split(",").map { it.trim() }.filter { it.isNotEmpty() },
    dailyTarget = dailyTarget,
    reminderTime = reminderTime,
    emailDailyNudge = emailDailyNudge,
    emailWeeklyDigest = emailWeeklyDigest,
    lessonModel = lessonModel,
    imagesEnabled = imagesEnabled,
    videoEnabled = videoEnabled
)

fun UserPrefs.toEntity(): UserPrefsEntity = UserPrefsEntity(
    userId = userId,
    displayName = displayName,
    email = email,
    avatarUrl = avatarUrl,
    preferredCategories = preferredCategories.joinToString(","),
    dailyTarget = dailyTarget,
    reminderTime = reminderTime,
    emailDailyNudge = emailDailyNudge,
    emailWeeklyDigest = emailWeeklyDigest,
    lessonModel = lessonModel,
    imagesEnabled = imagesEnabled,
    videoEnabled = videoEnabled
)
