package com.webagent.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecommendationDao {
    @Query("SELECT * FROM recommendations WHERE isRead = 0 ORDER BY priority DESC, timestamp DESC")
    fun getUnreadRecommendations(): Flow<List<Recommendation>>
    
    @Query("SELECT * FROM recommendations ORDER BY priority DESC, timestamp DESC LIMIT :limit")
    fun getRecentRecommendations(limit: Int = 50): Flow<List<Recommendation>>
    
    @Query("SELECT * FROM recommendations WHERE type = :type AND isRead = 0 ORDER BY priority DESC, timestamp DESC")
    fun getUnreadByType(type: RecommendationType): Flow<List<Recommendation>>
    
    @Insert
    suspend fun insertRecommendation(recommendation: Recommendation): Long
    
    @Update
    suspend fun updateRecommendation(recommendation: Recommendation)
    
    @Query("UPDATE recommendations SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)
    
    @Query("DELETE FROM recommendations WHERE id = :id")
    suspend fun deleteRecommendation(id: Long)
    
    @Query("DELETE FROM recommendations WHERE timestamp < :before")
    suspend fun deleteOldRecommendations(before: Long)
}
