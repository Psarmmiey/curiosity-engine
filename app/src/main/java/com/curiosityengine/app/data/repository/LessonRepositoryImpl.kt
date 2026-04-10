package com.curiosityengine.app.data.repository

import com.curiosityengine.app.data.local.dao.LessonDao
import com.curiosityengine.app.data.model.Lesson
import com.curiosityengine.app.data.remote.dto.LessonDto
import com.curiosityengine.app.data.remote.mapper.toDomain
import com.curiosityengine.app.data.remote.mapper.toEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepositoryImpl @Inject constructor(
    private val lessonDao: LessonDao,
    private val supabase: SupabaseClient
) : LessonRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override fun getLessonsForUser(userId: String): Flow<List<Lesson>> =
        lessonDao.getLessonsForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getLessonById(id: String): Flow<Lesson?> =
        lessonDao.getLessonById(id).map { it?.toDomain() }

    override fun getLessonForDate(date: String, userId: String): Flow<Lesson?> =
        lessonDao.getLessonForDate(date, userId).map { it?.toDomain() }

    override suspend fun getLessonByIdOnce(id: String): Lesson? =
        lessonDao.getLessonByIdOnce(id)?.toDomain()

    override suspend fun getLessonForDateOnce(date: String, userId: String): Lesson? =
        lessonDao.getLessonForDateOnce(date, userId)?.toDomain()

    override suspend fun fetchAndCacheTodaysLesson(userId: String, date: String): Lesson? {
        return runCatching {
            val dtos = supabase.postgrest
                .from("lessons")
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("date", date)
                    }
                }
                .decodeList<LessonDto>()

            dtos.firstOrNull()?.let { dto ->
                val entity = dto.toEntity()
                // Preserve local read state if we already have this lesson cached
                val existing = lessonDao.getLessonByIdOnce(dto.id)
                val merged = if (existing != null) {
                    entity.copy(isRead = existing.isRead, readAt = existing.readAt, quizScore = existing.quizScore)
                } else {
                    entity
                }
                lessonDao.insertOrReplace(merged)
                merged.toDomain()
            }
        }.getOrElse {
            // Network failed — fall back to cache
            lessonDao.getLessonForDateOnce(date, userId)?.toDomain()
        }
    }

    override suspend fun markAsRead(lessonId: String) {
        lessonDao.markAsRead(lessonId, System.currentTimeMillis())
    }

    override suspend fun saveQuizScore(lessonId: String, score: Int) {
        lessonDao.updateQuizScore(lessonId, score)
    }

    override fun getLessonsByCategory(category: String, userId: String): Flow<List<Lesson>> =
        lessonDao.getLessonsByCategory(category, userId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getUnreadLessons(userId: String): Flow<List<Lesson>> =
        lessonDao.getUnreadLessons(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getReadCount(userId: String): Flow<Int> =
        lessonDao.getReadCount(userId)

    override fun getBonusLessons(userId: String): Flow<List<Lesson>> =
        lessonDao.getBonusLessons(userId).map { entities ->
            entities.map { it.toDomain() }
        }
}
