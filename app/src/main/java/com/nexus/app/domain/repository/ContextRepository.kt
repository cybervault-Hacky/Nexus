package com.nexus.app.domain.repository

import com.nexus.app.domain.model.NexusContext
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over context persistence.
 * The UI layer depends only on this interface — never on Room directly.
 */
interface ContextRepository {

    /** Observe all contexts, ordered by most-recently updated. */
    fun observeAll(): Flow<List<NexusContext>>

    /** Observe a single context by its unique ID. Returns null if not found. */
    fun observeById(id: String): Flow<NexusContext?>

    /** Retrieve a single context once. Returns null if not found. */
    suspend fun getById(id: String): NexusContext?

    /** Insert a new context. Returns the assigned ID. */
    suspend fun insert(context: NexusContext): String

    /** Update an existing context. Throws if the ID does not exist. */
    suspend fun update(context: NexusContext)

    /** Delete a context by ID. No-op if the ID does not exist. */
    suspend fun delete(id: String)

    /**
     * Activate the given context and deactivate all others.
     * This enforces the single-active-context invariant at the data layer.
     */
    suspend fun activate(id: String)

    /** Deactivate the given context. */
    suspend fun deactivate(id: String)

    /** Observe the currently active context (at most one). */
    fun observeActive(): Flow<NexusContext?>
}
