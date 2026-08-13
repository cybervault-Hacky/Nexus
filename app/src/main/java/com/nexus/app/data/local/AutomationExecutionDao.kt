package com.nexus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationExecutionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AutomationExecutionEntity)

    @Query("SELECT * FROM automation_executions ORDER BY startedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<AutomationExecutionEntity>>

    @Query("SELECT * FROM automation_executions WHERE automationId = :automationId ORDER BY startedAt DESC LIMIT :limit")
    fun observeForAutomation(automationId: String, limit: Int = 20): Flow<List<AutomationExecutionEntity>>

    @Query("SELECT COUNT(*) FROM automation_executions")
    suspend fun count(): Int

    @Query("DELETE FROM automation_executions WHERE startedAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
