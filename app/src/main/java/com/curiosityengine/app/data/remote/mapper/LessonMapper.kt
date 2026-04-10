package com.curiosityengine.app.data.remote.mapper

import com.curiosityengine.app.data.local.entity.LessonEntity
import com.curiosityengine.app.data.model.Category
import com.curiosityengine.app.data.model.Lesson
import com.curiosityengine.app.data.model.LessonBlock
import com.curiosityengine.app.data.model.QuizQuestion
import com.curiosityengine.app.data.model.Reference
import com.curiosityengine.app.data.remote.dto.LessonDto
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; isLenient = true }

fun LessonDto.toEntity(): LessonEntity = LessonEntity(
    id = id,
    date = date,
    topic = topic,
    category = category,
    emoji = emoji,
    contentJson = contentJson,
    quizJson = quizJson,
    referencesJson = referencesJson,
    youtubeVideoId = youtubeVideoId,
    isBonus = isBonus,
    userId = userId,
    syncedAt = syncedAt
)

fun LessonEntity.toDomain(): Lesson {
    val blocks: List<LessonBlock> = runCatching {
        json.decodeFromString<List<LessonBlock>>(contentJson)
    }.getOrDefault(emptyList())

    val quiz: List<QuizQuestion> = runCatching {
        json.decodeFromString<List<QuizQuestion>>(quizJson)
    }.getOrDefault(emptyList())

    val references: List<Reference> = runCatching {
        json.decodeFromString<List<Reference>>(referencesJson)
    }.getOrDefault(emptyList())

    return Lesson(
        id = id,
        date = date,
        topic = topic,
        category = Category.fromDisplayName(category),
        emoji = emoji,
        blocks = blocks,
        quiz = quiz,
        references = references,
        youtubeVideoId = youtubeVideoId,
        isBonus = isBonus,
        isRead = isRead,
        readAt = readAt,
        quizScore = quizScore,
        userId = userId,
        syncedAt = syncedAt
    )
}
