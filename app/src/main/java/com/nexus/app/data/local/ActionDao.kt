package com.nexus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for action persistence.
 */
@Dao
interface ActionDao {

    @Query("SELECT * FROM actions WHERE contextId = :contextId ORDER BY position ASC")
    fun observeByContext(contextId: String): Flow<List<ActionEntity>>

    @Query("SELECT * FROM actions WHERE id = :id")
    fun observeById(id: String): Flow<ActionEntity?>

    @Query("SELECT * FROM actions WHERE id = :id")
    suspend fun getById(id: String): ActionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ActionEntity)

    @Update
    suspend fun update(entity: ActionEntity)

    @Query("DELETE FROM actions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM actions WHERE contextId = :contextId")
    suspend fun deleteAllForContext(contextId: String)

    @Query("UPDATE actions SET isEnabled = :isEnabled, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setEnabled(id: String, isEnabled: Boolean, updatedAt: Long)

    @Query("UPDATE actions SET position = :position, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setPosition(id: String, position: Int, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM actions WHERE contextId = :contextId")
    fun observeCount(contextId: String): Flow<Int>

    @Query("SELECT * FROM actions WHERE contextId = :contextId ORDER BY position ASC")
    suspend fun getByContext(contextId: String): List<ActionEntity>

    @Query("SELECT COUNT(*) FROM actions WHERE contextId = :contextId")
    suspend fun getCount(contextId: String): Int
}
