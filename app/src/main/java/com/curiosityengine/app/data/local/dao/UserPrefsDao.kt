package com.curiosityengine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.curiosityengine.app.data.local.entity.UserPrefsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPrefsDao {

    @Query("SELECT * FROM user_prefs WHERE user_id = :userId")
    fun getUserPrefs(userId: String): Flow<UserPrefsEntity?>

    @Query("SELECT * FROM user_prefs WHERE user_id = :userId")
    suspend fun getUserPrefsOnce(userId: String): UserPrefsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(userPrefs: UserPrefsEntity)

    @Update
    suspend fun update(userPrefs: UserPrefsEntity)

    @Query("DELETE FROM user_prefs WHERE user_id = :userId")
    suspend fun delete(userId: String)

    @Query("DELETE FROM user_prefs")
    suspend fun deleteAll()
}
