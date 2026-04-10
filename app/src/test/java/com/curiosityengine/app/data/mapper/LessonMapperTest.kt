package com.curiosityengine.app.data.mapper

import com.curiosityengine.app.data.local.entity.LessonEntity
import com.curiosityengine.app.data.model.Category
import com.curiosityengine.app.data.model.LessonBlock
import com.curiosityengine.app.data.remote.dto.LessonDto
import com.curiosityengine.app.data.remote.mapper.toEntity
import com.curiosityengine.app.data.remote.mapper.toDomain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonMapperTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun makeDto(
        contentJson: String = "[]",
        quizJson: String = "[]",
        referencesJson: String = "[]",
        category: String = "Science",
    ) = LessonDto(
        id = "lesson-1",
        date = "2026-04-10",
        topic = "Test Topic",
        category = category,
        emoji = "🔬",
        contentJson = contentJson,
        quizJson = quizJson,
        referencesJson = referencesJson,
    )

    private fun makeEntity(
        contentJson: String = "[]",
        quizJson: String = "[]",
        referencesJson: String = "[]",
        category: String = "Science",
    ) = LessonEntity(
        id = "lesson-1",
        date = "2026-04-10",
        topic = "Test Topic",
        category = category,
        emoji = "🔬",
        contentJson = contentJson,
        quizJson = quizJson,
        referencesJson = referencesJson,
    )

    // -----------------------------------------------------------------------
    // LessonDto.toEntity() tests
    // -----------------------------------------------------------------------

    @Test
    fun `toEntity preserves contentJson as-is`() {
        val blocks = listOf(LessonBlock.Text(markdown = "Hello **world**"))
        val encoded = json.encodeToString(blocks)
        val dto = makeDto(contentJson = encoded)

        val entity = dto.toEntity()

        assertEquals(encoded, entity.contentJson)
    }

    @Test
    fun `toEntity preserves category string`() {
        val dto = makeDto(category = "Technology")
        val entity = dto.toEntity()
        assertEquals("Technology", entity.category)
    }

    @Test
    fun `toEntity preserves quizJson as-is`() {
        val dto = makeDto(quizJson = "[]")
        val entity = dto.toEntity()
        assertEquals("[]", entity.quizJson)
    }

    // -----------------------------------------------------------------------
    // LessonEntity.toDomain() tests
    // -----------------------------------------------------------------------

    @Test
    fun `toDomain decodes contentJson blocks correctly`() {
        val blocks = listOf(
            LessonBlock.Text(markdown = "## Section"),
            LessonBlock.Quote(text = "Be curious", attribution = "Einstein"),
        )
        val encoded = json.encodeToString(blocks)
        val entity = makeEntity(contentJson = encoded)

        val domain = entity.toDomain()

        assertEquals(2, domain.blocks.size)
        assertTrue(domain.blocks[0] is LessonBlock.Text)
        assertTrue(domain.blocks[1] is LessonBlock.Quote)
        assertEquals("## Section", (domain.blocks[0] as LessonBlock.Text).markdown)
    }

    @Test
    fun `toDomain returns empty quiz list when quizJson is empty array`() {
        val entity = makeEntity(quizJson = "[]")
        val domain = entity.toDomain()
        assertTrue(domain.quiz.isEmpty())
    }

    @Test
    fun `toDomain returns empty quiz list when quizJson is null-like malformed`() {
        // An invalid JSON string should fall back to emptyList via runCatching
        val entity = makeEntity(quizJson = "null")
        val domain = entity.toDomain()
        assertTrue(domain.quiz.isEmpty())
    }

    @Test
    fun `toDomain maps category display name to Category enum`() {
        val entity = makeEntity(category = "Psychology")
        val domain = entity.toDomain()
        assertEquals(Category.PSYCHOLOGY, domain.category)
    }

    @Test
    fun `toDomain maps unknown category to SURPRISE_ME`() {
        val entity = makeEntity(category = "Unknown Category XYZ")
        val domain = entity.toDomain()
        assertEquals(Category.SURPRISE_ME, domain.category)
    }

    @Test
    fun `contentJson roundtrips through toEntity then toDomain`() {
        val blocks = listOf(
            LessonBlock.Text(markdown = "Intro paragraph"),
            LessonBlock.Slide(title = "Key Idea", body = "Details here"),
        )
        val encoded = json.encodeToString(blocks)
        val dto = makeDto(contentJson = encoded)

        // Roundtrip: Dto → Entity → Domain
        val domain = dto.toEntity().toDomain()

        assertEquals(2, domain.blocks.size)
        assertTrue(domain.blocks[0] is LessonBlock.Text)
        assertTrue(domain.blocks[1] is LessonBlock.Slide)
        assertEquals("Intro paragraph", (domain.blocks[0] as LessonBlock.Text).markdown)
        assertEquals("Key Idea", (domain.blocks[1] as LessonBlock.Slide).title)
    }
}
