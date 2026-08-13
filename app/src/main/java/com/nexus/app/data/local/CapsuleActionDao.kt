package com.nexus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for capsule action snapshots.
 */
@Dao
interface CapsuleActionDao {

    @Query("SELECT * FROM capsule_actions WHERE capsuleId = :capsuleId ORDER BY position ASC")
    fun observeByCapsule(capsuleId: String): Flow<List<CapsuleActionEntity>>

    @Query("SELECT * FROM capsule_actions WHERE capsuleId = :capsuleId ORDER BY position ASC")
    suspend fun getByCapsule(capsuleId: String): List<CapsuleActionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: CapsuleActionEntity)

    @Query("DELETE FROM capsule_actions WHERE capsuleId = :capsuleId")
    suspend fun deleteAllForCapsule(capsuleId: String)
}
