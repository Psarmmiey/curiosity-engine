package com.curiosityengine.app.data.repository

import com.curiosityengine.app.data.local.dao.LessonDao
import com.curiosityengine.app.data.model.QuizQuestion
import com.curiosityengine.app.data.model.QuizResult
import com.curiosityengine.app.network.SupabaseEdgeFunctions
import io.github.jan.supabase.SupabaseClient
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val lessonDao: LessonDao
) : QuizRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun getQuizForLesson(lessonId: String, userId: String): List<QuizQuestion> {
        val responseStr = SupabaseEdgeFunctions.generateQuiz(
            client = supabase,
            lessonId = lessonId,
            userId = userId
        )
        return runCatching {
            json.decodeFromString<List<QuizQuestion>>(responseStr)
        }.getOrElse {
            // Fallback: parse quiz from locally cached lesson
            lessonDao.getLessonByIdOnce(lessonId)?.let { entity ->
                runCatching {
                    json.decodeFromString<List<QuizQuestion>>(entity.quizJson)
                }.getOrDefault(emptyList())
            } ?: emptyList()
        }
    }

    override suspend fun getWeeklyQuiz(userId: String): List<QuizQuestion> {
        val responseStr = SupabaseEdgeFunctions.generateWeeklyQuiz(
            client = supabase,
            userId = userId
        )
        return runCatching {
            json.decodeFromString<List<QuizQuestion>>(responseStr)
        }.getOrDefault(emptyList())
    }

    override suspend fun submitQuizResult(result: QuizResult) {
        // Persist score locally
        lessonDao.updateQuizScore(result.lessonId, result.score)

        // Best-effort remote persistence — silently ignore failures
        runCatching {
            // Quiz results are submitted as a Supabase Postgrest insert in a future iteration.
            // For now, the local persistence above is the source of truth.
        }
    }
}
