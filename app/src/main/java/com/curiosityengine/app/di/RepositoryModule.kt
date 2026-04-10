package com.curiosityengine.app.di

import com.curiosityengine.app.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindLessonRepository(impl: LessonRepositoryImpl): LessonRepository

    @Binds @Singleton
    abstract fun bindQuizRepository(impl: QuizRepositoryImpl): QuizRepository

    @Binds @Singleton
    abstract fun bindStreakRepository(impl: StreakRepositoryImpl): StreakRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}
