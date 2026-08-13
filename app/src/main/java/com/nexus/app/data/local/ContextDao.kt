package com.nexus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for context persistence.
 *
 * All read operations return [Flow] so the UI updates automatically
 * when data changes — no manual refresh needed.
 */
@Dao
interface ContextDao {

    @Query("SELECT * FROM contexts ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ContextEntity>>

    @Query("SELECT * FROM contexts WHERE id = :id")
    fun observeById(id: String): Flow<ContextEntity?>

    @Query("SELECT * FROM contexts WHERE id = :id")
    suspend fun getById(id: String): ContextEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ContextEntity)

    @Update
    suspend fun update(entity: ContextEntity)

    @Query("DELETE FROM contexts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE contexts SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE contexts SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setActive(id: String, isActive: Boolean, updatedAt: Long)

    @Query("SELECT * FROM contexts WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<ContextEntity?>

    @Query("SELECT * FROM contexts WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): ContextEntity?
}
