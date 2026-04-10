package com.curiosityengine.app.data.repository

import com.curiosityengine.app.data.model.QuizQuestion
import com.curiosityengine.app.data.model.QuizResult

interface QuizRepository {

    /**
     * Fetch quiz questions for a lesson from the Supabase Edge Function.
     * Results are not cached — always fetched fresh from the network.
     */
    suspend fun getQuizForLesson(lessonId: String, userId: String): List<QuizQuestion>

    /**
     * Fetch a weekly review quiz that covers the last 7 days of lessons.
     */
    suspend fun getWeeklyQuiz(userId: String): List<QuizQuestion>

    /**
     * Submit a completed quiz result to Supabase and persist the score locally.
     */
    suspend fun submitQuizResult(result: QuizResult)
}
