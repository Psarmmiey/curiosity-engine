package com.curiosityengine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.curiosityengine.app.data.local.entity.StreakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {

    @Query("SELECT * FROM streaks WHERE user_id = :userId")
    fun getStreak(userId: String): Flow<StreakEntity?>

    @Query("SELECT * FROM streaks WHERE user_id = :userId")
    suspend fun getStreakOnce(userId: String): StreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(streak: StreakEntity)

    @Update
    suspend fun update(streak: StreakEntity)

    @Query("DELETE FROM streaks WHERE user_id = :userId")
    suspend fun deleteByUserId(userId: String)
}
