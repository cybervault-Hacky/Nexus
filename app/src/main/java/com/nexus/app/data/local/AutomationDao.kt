package com.nexus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationDao {

    @Query("SELECT * FROM automation_rules ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<AutomationEntity>>

    @Query("SELECT * FROM automation_rules WHERE isEnabled = 1")
    fun observeEnabled(): Flow<List<AutomationEntity>>

    @Query("SELECT * FROM automation_rules WHERE id = :id")
    fun observeById(id: String): Flow<AutomationEntity?>

    @Query("SELECT * FROM automation_rules WHERE id = :id")
    suspend fun getById(id: String): AutomationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AutomationEntity)

    @Update
    suspend fun update(entity: AutomationEntity)

    @Query("DELETE FROM automation_rules WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE automation_rules SET isEnabled = :enabled, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean, updatedAt: Long)

    @Query("UPDATE automation_rules SET lastTriggeredAt = :timestamp, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateLastTriggeredAt(id: String, timestamp: Long)

    @Query("SELECT * FROM automation_rules WHERE triggerType = :type AND isEnabled = 1")
    suspend fun getEnabledByTriggerType(type: String): List<AutomationEntity>

    @Query("SELECT * FROM automation_rules WHERE triggerType = :type AND isEnabled = 1")
    fun observeEnabledByTriggerType(type: String): Flow<List<AutomationEntity>>

    @Query("SELECT COUNT(*) FROM automation_rules")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM automation_rules WHERE isEnabled = 1")
    fun observeEnabledCount(): Flow<Int>

    // Phase 10
    @Query("UPDATE automation_rules SET priority = :priority, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePriority(id: String, priority: Int, updatedAt: Long)

    @Query("UPDATE automation_rules SET healthStatus = :health, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateHealth(id: String, health: String, updatedAt: Long)

    @Query("UPDATE automation_rules SET executionCount = executionCount + 1, successCount = successCount + 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun recordSuccess(id: String, updatedAt: Long)

    @Query("UPDATE automation_rules SET executionCount = executionCount + 1, failureCount = failureCount + 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun recordFailure(id: String, updatedAt: Long)

    @Query("SELECT * FROM automation_rules")
    suspend fun getAllRules(): List<AutomationEntity>
}
