package com.curiosityengine.app.util

object Constants {
    // Deep link scheme
    const val DEEP_LINK_SCHEME = "curiosityengine"

    // Notification channel IDs
    const val CHANNEL_LESSON_READY = "lesson_ready"
    const val CHANNEL_STREAK_RISK = "streak_risk"
    const val CHANNEL_WEEKLY_REVIEW = "weekly_review"
    const val CHANNEL_MILESTONE = "milestone"
    const val CHANNEL_KEEP_LEARNING = "keep_learning"

    // WorkManager tags
    const val WORK_LESSON_PREFETCH = "lesson_prefetch"
    const val WORK_STREAK_SYNC = "streak_sync"
    const val WORK_WIDGET_UPDATE = "widget_update"

    // Room DB
    const val DB_NAME = "curiosity_engine.db"
    const val DB_VERSION = 1

    // Lesson
    const val MAX_OFFLINE_LESSONS = 7
    const val LESSON_COMPLETE_SCROLL_PERCENT = 0.90f

    // Doom scroll
    const val DOOM_SCROLL_PREFETCH_THRESHOLD = 2  // prefetch when queue drops below this

    // Share
    const val SHARE_CARD_WIDTH = 1080
    const val SHARE_CARD_HEIGHT = 1080
    const val FILE_PROVIDER_AUTHORITY = "com.curiosityengine.app.fileprovider"

    // Categories
    val CATEGORIES = listOf(
        "Science", "Nature", "History", "Psychology",
        "Technology", "Art & Culture", "Economics", "Philosophy", "Surprise Me"
    )
}
