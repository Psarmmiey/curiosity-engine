package com.curiosityengine.app.data.repository

import com.curiosityengine.app.data.model.Streak
import kotlinx.coroutines.flow.Flow

interface StreakRepository {

    /** Observe the streak for the given user. */
    fun getStreak(userId: String): Flow<Streak?>

    /** One-shot read of the streak. */
    suspend fun getStreakOnce(userId: String): Streak?

    /**
     * Record that the user completed a lesson on [date] (ISO yyyy-MM-dd).
     * Updates chain streak, best streak, weekly streak, and week days done, then
     * persists locally and syncs to Supabase best-effort.
     */
    suspend fun recordCompletion(userId: String, date: String)

    /**
     * Fetch the latest streak from Supabase, merge with local state, and persist.
     */
    suspend fun syncStreak(userId: String)

    /** Overwrite the local streak with the given value (used after remote sync). */
    suspend fun saveStreak(streak: Streak)
}
