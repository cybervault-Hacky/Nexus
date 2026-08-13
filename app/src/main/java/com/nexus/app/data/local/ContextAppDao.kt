package com.nexus.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the context ↔ app relationship.
 *
 * The foreign key with CASCADE ensures that deleting a Context
 * automatically removes its associated app records.
 */
@Dao
interface ContextAppDao {

    /** Observe all app associations for a context. */
    @Query("SELECT * FROM context_apps WHERE contextId = :contextId ORDER BY packageName ASC")
    fun observeByContext(contextId: String): Flow<List<ContextAppEntity>>

    /** Observe the count of apps for a context. */
    @Query("SELECT COUNT(*) FROM context_apps WHERE contextId = :contextId")
    fun observeCount(contextId: String): Flow<Int>

    /** Get all app associations for a context (non-reactive). */
    @Query("SELECT * FROM context_apps WHERE contextId = :contextId")
    suspend fun getByContext(contextId: String): List<ContextAppEntity>

    /** Get all package names for a context. */
    @Query("SELECT packageName FROM context_apps WHERE contextId = :contextId")
    suspend fun getPackageNames(contextId: String): List<String>

    /** Check if a package is already associated with a context. */
    @Query(
        "SELECT COUNT(*) FROM context_apps WHERE contextId = :contextId AND packageName = :packageName"
    )
    suspend fun count(contextId: String, packageName: String): Int

    /** Insert a context-app association. IGNORE on conflict (duplicate). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ContextAppEntity)

    /** Remove a specific association. */
    @Query("DELETE FROM context_apps WHERE contextId = :contextId AND packageName = :packageName")
    suspend fun delete(contextId: String, packageName: String)

    /** Remove all associations for a context. */
    @Query("DELETE FROM context_apps WHERE contextId = :contextId")
    suspend fun deleteAllForContext(contextId: String)

    /** Remove all associations for a context and insert new ones (transaction). */
    @Query("DELETE FROM context_apps WHERE contextId = :contextId")
    suspend fun clearContext(contextId: String)
}
