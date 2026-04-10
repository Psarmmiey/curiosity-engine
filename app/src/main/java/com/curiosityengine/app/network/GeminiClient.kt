package com.curiosityengine.app.network

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import timber.log.Timber

/**
 * Low-level Gemini API client.
 * NOTE: In production, all AI calls go through Supabase Edge Functions so API keys
 * never reach the device. This client is available for local development/testing only.
 * The DI module wires this with an empty key in release builds.
 */
class GeminiClient(private val apiKey: String) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) { Timber.tag("GeminiClient").v(message) }
            }
            level = LogLevel.HEADERS
        }
    }

    private fun baseUrl(model: GeminiModel) =
        "https://generativelanguage.googleapis.com/v1beta/models/${model.modelId}:generateContent"

    suspend fun generateContent(
        prompt: String,
        model: GeminiModel = GeminiModel.DEFAULT,
        temperature: Float = 0.7f
    ): String {
        val response = httpClient.post(baseUrl(model)) {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "contents" to listOf(mapOf("parts" to listOf(mapOf("text" to prompt)))),
                    "generationConfig" to mapOf("temperature" to temperature)
                )
            )
        }
        return response.bodyAsText()
    }

    fun close() = httpClient.close()
}
