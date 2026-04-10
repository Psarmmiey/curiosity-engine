package com.curiosityengine.app.sync

import com.curiosityengine.app.data.model.Streak
import org.junit.Assert.assertEquals
import org.junit.Test

class ConflictResolverTest {

    private fun streak(updatedAt: Long) = Streak(
        userId = "user1",
        chainStreak = 0,
        bestStreak = 0,
        updatedAt = updatedAt,
    )

    // Both updatedAt are zero (treated as absent) → returns remote
    @Test
    fun `both updatedAt absent returns remote`() {
        val local = streak(0L)
        val remote = streak(0L).copy(chainStreak = 5)
        val result = ConflictResolver.resolveStreak(local, remote)
        assertEquals(remote, result)
    }

    // Local updatedAt is zero (absent) → returns remote
    @Test
    fun `local updatedAt absent returns remote`() {
        val local = streak(0L)
        val remote = streak(1000L).copy(chainStreak = 3)
        val result = ConflictResolver.resolveStreak(local, remote)
        assertEquals(remote, result)
    }

    // Remote updatedAt is zero (absent) → returns local
    @Test
    fun `remote updatedAt absent returns local`() {
        val local = streak(1000L).copy(chainStreak = 7)
        val remote = streak(0L)
        val result = ConflictResolver.resolveStreak(local, remote)
        assertEquals(local, result)
    }

    // Local is newer → returns local
    @Test
    fun `local newer returns local`() {
        val local = streak(2000L).copy(chainStreak = 10)
        val remote = streak(1000L).copy(chainStreak = 5)
        val result = ConflictResolver.resolveStreak(local, remote)
        assertEquals(local, result)
    }

    // Remote is newer → returns remote
    @Test
    fun `remote newer returns remote`() {
        val local = streak(1000L).copy(chainStreak = 5)
        val remote = streak(2000L).copy(chainStreak = 10)
        val result = ConflictResolver.resolveStreak(local, remote)
        assertEquals(remote, result)
    }

    // Equal timestamps → returns remote (tie-break to remote)
    @Test
    fun `equal timestamps returns remote`() {
        val local = streak(1500L).copy(chainStreak = 4)
        val remote = streak(1500L).copy(chainStreak = 8)
        val result = ConflictResolver.resolveStreak(local, remote)
        assertEquals(remote, result)
    }
}
