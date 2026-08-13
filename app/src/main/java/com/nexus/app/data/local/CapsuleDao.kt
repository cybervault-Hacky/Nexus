package com.nexus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for capsule persistence.
 */
@Dao
interface CapsuleDao {

    @Query("SELECT * FROM capsules ORDER BY capturedAt DESC")
    fun observeAll(): Flow<List<CapsuleEntity>>

    @Query("SELECT * FROM capsules WHERE id = :id")
    fun observeById(id: String): Flow<CapsuleEntity?>

    @Query("SELECT * FROM capsules WHERE id = :id")
    suspend fun getById(id: String): CapsuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CapsuleEntity)

    @Update
    suspend fun update(entity: CapsuleEntity)

    @Query("DELETE FROM capsules WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE capsules SET name = :newName WHERE id = :id")
    suspend fun updateName(id: String, newName: String)

    @Query("UPDATE capsules SET description = :newDescription WHERE id = :id")
    suspend fun updateDescription(id: String, newDescription: String)

    @Query("SELECT * FROM capsules WHERE sourceContextId = :contextId ORDER BY capturedAt DESC")
    fun observeByContext(contextId: String): Flow<List<CapsuleEntity>>

    @Query("SELECT COUNT(*) FROM capsules WHERE sourceContextId = :contextId")
    fun observeCountByContext(contextId: String): Flow<Int>

    @Query("""
        SELECT * FROM capsules
        WHERE name LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY capturedAt DESC
    """)
    fun search(query: String): Flow<List<CapsuleEntity>>
}
