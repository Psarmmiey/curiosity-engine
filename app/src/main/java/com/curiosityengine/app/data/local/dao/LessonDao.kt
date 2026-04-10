package com.curiosityengine.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.curiosityengine.app.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    @Query("SELECT * FROM lessons ORDER BY date DESC")
    fun getAllLessons(): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE user_id = :userId ORDER BY date DESC")
    fun getLessonsForUser(userId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    fun getLessonById(id: String): Flow<LessonEntity?>

    @Query("SELECT * FROM lessons WHERE id = :id")
    suspend fun getLessonByIdOnce(id: String): LessonEntity?

    @Query("SELECT * FROM lessons WHERE date = :date AND user_id = :userId LIMIT 1")
    fun getLessonForDate(date: String, userId: String): Flow<LessonEntity?>

    @Query("SELECT * FROM lessons WHERE date = :date AND user_id = :userId LIMIT 1")
    suspend fun getLessonForDateOnce(date: String, userId: String): LessonEntity?

    @Query("SELECT * FROM lessons WHERE is_read = 0 AND user_id = :userId ORDER BY date DESC")
    fun getUnreadLessons(userId: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE category = :category AND user_id = :userId ORDER BY date DESC")
    fun getLessonsByCategory(category: String, userId: String): Flow<List<LessonEntity>>

    @Query("SELECT COUNT(*) FROM lessons WHERE is_read = 1 AND user_id = :userId")
    fun getReadCount(userId: String): Flow<Int>

    @Query("SELECT * FROM lessons WHERE is_bonus = 1 AND user_id = :userId ORDER BY date DESC")
    fun getBonusLessons(userId: String): Flow<List<LessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(lesson: LessonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceAll(lessons: List<LessonEntity>)

    @Update
    suspend fun update(lesson: LessonEntity)

    @Query("UPDATE lessons SET is_read = 1, read_at = :readAt WHERE id = :id")
    suspend fun markAsRead(id: String, readAt: Long)

    @Query("UPDATE lessons SET quiz_score = :score WHERE id = :id")
    suspend fun updateQuizScore(id: String, score: Int)

    @Query("DELETE FROM lessons WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM lessons WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("SELECT * FROM lessons WHERE user_id = :userId ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentLessons(userId: String, limit: Int): List<LessonEntity>
}
