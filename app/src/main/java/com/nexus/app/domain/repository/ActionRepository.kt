package com.nexus.app.domain.repository

import com.nexus.app.domain.model.NexusAction
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over action persistence.
 * The UI layer depends only on this interface.
 */
interface ActionRepository {

    /** Observe all actions for a context, ordered by position. */
    fun observeActionsForContext(contextId: String): Flow<List<NexusAction>>

    /** Observe a single action by ID. */
    fun observeById(id: String): Flow<NexusAction?>

    /** Get a single action by ID (non-reactive). */
    suspend fun getById(id: String): NexusAction?

    /** Create a new action. Returns the assigned ID. */
    suspend fun insert(action: NexusAction): String

    /** Update an existing action. */
    suspend fun update(action: NexusAction)

    /** Delete an action by ID. */
    suspend fun delete(id: String)

    /** Delete all actions for a context. */
    suspend fun deleteAllForContext(contextId: String)

    /** Toggle enabled/disabled state. */
    suspend fun setEnabled(id: String, isEnabled: Boolean)

    /** Reorder actions. The list should be in the desired order. */
    suspend fun reorder(contextId: String, orderedIds: List<String>)

    /** Observe the count of actions for a context. */
    fun observeActionCount(contextId: String): Flow<Int>

    /** Get all actions for a context once (non-reactive). */
    suspend fun getActionsForContext(contextId: String): List<NexusAction>
}
