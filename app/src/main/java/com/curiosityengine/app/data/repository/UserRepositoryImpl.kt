package com.curiosityengine.app.data.repository

import com.curiosityengine.app.data.local.dao.UserPrefsDao
import com.curiosityengine.app.data.model.UserPrefs
import com.curiosityengine.app.data.remote.dto.UserDto
import com.curiosityengine.app.data.remote.mapper.toDomain
import com.curiosityengine.app.data.remote.mapper.toEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userPrefsDao: UserPrefsDao,
    private val supabase: SupabaseClient
) : UserRepository {

    override fun getUserPrefs(userId: String): Flow<UserPrefs?> =
        userPrefsDao.getUserPrefs(userId).map { it?.toDomain() }

    override suspend fun getUserPrefsOnce(userId: String): UserPrefs? =
        userPrefsDao.getUserPrefsOnce(userId)?.toDomain()

    override suspend fun fetchAndCacheUserPrefs(userId: String): UserPrefs? {
        return runCatching {
            val dtos = supabase.postgrest
                .from("user_prefs")
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<UserDto>()

            dtos.firstOrNull()?.let { dto ->
                val entity = dto.toEntity()
                userPrefsDao.insertOrUpdate(entity)
                entity.toDomain()
            }
        }.getOrElse {
            // Network failed — return cached value
            userPrefsDao.getUserPrefsOnce(userId)?.toDomain()
        }
    }

    override suspend fun saveUserPrefs(prefs: UserPrefs) {
        userPrefsDao.insertOrUpdate(prefs.toEntity())
        runCatching { pushUserPrefsToSupabase(prefs) }
    }

    override suspend fun updateUserPrefs(prefs: UserPrefs) {
        userPrefsDao.insertOrUpdate(prefs.toEntity())
        runCatching { pushUserPrefsToSupabase(prefs) }
    }

    override suspend fun clearUserPrefs(userId: String) {
        userPrefsDao.delete(userId)
    }

    // ---------------------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------------------

    private suspend fun pushUserPrefsToSupabase(prefs: UserPrefs) {
        supabase.postgrest
            .from("user_prefs")
            .upsert(
                value = UserDto(
                    userId = prefs.userId,
                    displayName = prefs.displayName,
                    email = prefs.email,
                    avatarUrl = prefs.avatarUrl,
                    preferredCategories = prefs.preferredCategories,
                    dailyTarget = prefs.dailyTarget,
                    reminderTime = prefs.reminderTime,
                    emailDailyNudge = prefs.emailDailyNudge,
                    emailWeeklyDigest = prefs.emailWeeklyDigest,
                    lessonModel = prefs.lessonModel,
                    imagesEnabled = prefs.imagesEnabled,
                    videoEnabled = prefs.videoEnabled
                )
            )
    }
}
