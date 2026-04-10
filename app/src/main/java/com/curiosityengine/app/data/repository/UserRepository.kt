package com.curiosityengine.app.data.repository

import com.curiosityengine.app.data.model.UserPrefs
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    /** Observe user preferences for the given user ID. */
    fun getUserPrefs(userId: String): Flow<UserPrefs?>

    /** One-shot read of user preferences. */
    suspend fun getUserPrefsOnce(userId: String): UserPrefs?

    /**
     * Fetch user preferences from Supabase, persist locally, and return them.
     * Falls back to the local cache if the network call fails.
     */
    suspend fun fetchAndCacheUserPrefs(userId: String): UserPrefs?

    /**
     * Persist updated preferences locally and sync to Supabase best-effort.
     */
    suspend fun saveUserPrefs(prefs: UserPrefs)

    /**
     * Update a subset of user preferences on Supabase and persist locally.
     */
    suspend fun updateUserPrefs(prefs: UserPrefs)

    /** Delete all local preferences for the user (e.g. on sign-out). */
    suspend fun clearUserPrefs(userId: String)
}
