package com.curiosityengine.app.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.request.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Typed wrappers for all Supabase Edge Function calls.
 * API keys for Gemini, Claude, YouTube, and Resend live in Supabase Vault —
 * only the Supabase Anon Key is ever on the device.
 */
object SupabaseEdgeFunctions {

    suspend fun generateLesson(
        client: SupabaseClient,
        userId: String,
        category: String,
        date: String,
        model: String = "gemini-2.5-pro"
    ): String {
        val body = buildJsonObject {
            put("userId", userId)
            put("category", category)
            put("date", date)
            put("model", model)
        }
        val response = client.functions.invoke(
            function = "generate-lesson",
            body = body
        )
        return response.toString()
    }

    suspend fun generateQuiz(
        client: SupabaseClient,
        lessonId: String,
        userId: String
    ): String {
        val body = buildJsonObject {
            put("lessonId", lessonId)
            put("userId", userId)
        }
        val response = client.functions.invoke(
            function = "generate-quiz",
            body = body
        )
        return response.toString()
    }

    suspend fun generateWeeklyQuiz(
        client: SupabaseClient,
        userId: String
    ): String {
        val body = buildJsonObject {
            put("userId", userId)
        }
        val response = client.functions.invoke(
            function = "generate-weekly-quiz",
            body = body
        )
        return response.toString()
    }
}
