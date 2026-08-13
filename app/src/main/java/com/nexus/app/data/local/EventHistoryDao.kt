package com.nexus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: EventHistoryEntity)

    @Query("SELECT * FROM event_history ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<EventHistoryEntity>>

    @Query("SELECT COUNT(*) FROM event_history")
    suspend fun count(): Int

    @Query("DELETE FROM event_history WHERE id NOT IN (SELECT id FROM event_history ORDER BY timestamp DESC LIMIT :keep)")
    suspend fun keepMostRecent(keep: Int = 500)

    @Query("DELETE FROM event_history")
    suspend fun deleteAll()
}
