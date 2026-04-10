package com.curiosityengine.app.data.repository

import com.curiosityengine.app.data.model.Lesson
import kotlinx.coroutines.flow.Flow

interface LessonRepository {

    /** Observe all lessons for a user, ordered newest-first. */
    fun getLessonsForUser(userId: String): Flow<List<Lesson>>

    /** Observe a single lesson by its ID. */
    fun getLessonById(id: String): Flow<Lesson?>

    /** Observe the lesson for a specific ISO date and user. */
    fun getLessonForDate(date: String, userId: String): Flow<Lesson?>

    /** One-shot fetch of a lesson by ID (no cache fallback). */
    suspend fun getLessonByIdOnce(id: String): Lesson?

    /** One-shot fetch of the lesson for a date (local only). */
    suspend fun getLessonForDateOnce(date: String, userId: String): Lesson?

    /**
     * Fetch today's lesson from Supabase, persist it locally, and return it.
     * Returns null if the network call fails and no cached copy exists.
     */
    suspend fun fetchAndCacheTodaysLesson(userId: String, date: String): Lesson?

    /** Mark a lesson as read and record the timestamp. */
    suspend fun markAsRead(lessonId: String)

    /** Persist a quiz score for a lesson. */
    suspend fun saveQuizScore(lessonId: String, score: Int)

    /** Observe lessons for a specific category. */
    fun getLessonsByCategory(category: String, userId: String): Flow<List<Lesson>>

    /** Observe unread lessons for a user. */
    fun getUnreadLessons(userId: String): Flow<List<Lesson>>

    /** Observe the count of read lessons. */
    fun getReadCount(userId: String): Flow<Int>

    /** Observe bonus lessons. */
    fun getBonusLessons(userId: String): Flow<List<Lesson>>
}
