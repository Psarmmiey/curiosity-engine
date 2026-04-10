package com.curiosityengine.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.curiosityengine.app.data.local.dao.LessonDao
import com.curiosityengine.app.data.local.dao.StreakDao
import com.curiosityengine.app.data.local.dao.UserPrefsDao
import com.curiosityengine.app.data.local.entity.LessonEntity
import com.curiosityengine.app.data.local.entity.StreakEntity
import com.curiosityengine.app.data.local.entity.UserPrefsEntity

@Database(
    entities = [LessonEntity::class, StreakEntity::class, UserPrefsEntity::class],
    version = 1,
    exportSchema = true
)
abstract class CuriosityDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun streakDao(): StreakDao
    abstract fun userPrefsDao(): UserPrefsDao
}
