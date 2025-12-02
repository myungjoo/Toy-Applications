package com.webagent.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int = 100): Flow<List<EventData>>
    
    @Query("SELECT * FROM events WHERE type = :type ORDER BY timestamp DESC LIMIT :limit")
    fun getEventsByType(type: EventType, limit: Int = 100): Flow<List<EventData>>
    
    @Query("SELECT * FROM events WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getEventsSince(since: Long): Flow<List<EventData>>
    
    @Insert
    suspend fun insertEvent(event: EventData): Long
    
    @Insert
    suspend fun insertEvents(events: List<EventData>)
    
    @Query("DELETE FROM events WHERE timestamp < :before")
    suspend fun deleteOldEvents(before: Long)
    
    @Query("SELECT COUNT(*) FROM events")
    suspend fun getEventCount(): Int
}
