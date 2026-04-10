package com.curiosityengine.app.di

import android.content.Context
import androidx.room.Room
import com.curiosityengine.app.data.local.CuriosityDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CuriosityDatabase =
        Room.databaseBuilder(
            context,
            CuriosityDatabase::class.java,
            "curiosity_engine.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideLessonDao(db: CuriosityDatabase) = db.lessonDao()

    @Provides
    fun provideStreakDao(db: CuriosityDatabase) = db.streakDao()

    @Provides
    fun provideUserPrefsDao(db: CuriosityDatabase) = db.userPrefsDao()
}
