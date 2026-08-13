package com.nexus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for capsule app snapshots.
 */
@Dao
interface CapsuleAppDao {

    @Query("SELECT * FROM capsule_apps WHERE capsuleId = :capsuleId ORDER BY position ASC")
    fun observeByCapsule(capsuleId: String): Flow<List<CapsuleAppEntity>>

    @Query("SELECT * FROM capsule_apps WHERE capsuleId = :capsuleId ORDER BY position ASC")
    suspend fun getByCapsule(capsuleId: String): List<CapsuleAppEntity>

    @Query("SELECT packageName FROM capsule_apps WHERE capsuleId = :capsuleId ORDER BY position ASC")
    suspend fun getPackageNames(capsuleId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: CapsuleAppEntity)

    @Query("DELETE FROM capsule_apps WHERE capsuleId = :capsuleId")
    suspend fun deleteAllForCapsule(capsuleId: String)
}
