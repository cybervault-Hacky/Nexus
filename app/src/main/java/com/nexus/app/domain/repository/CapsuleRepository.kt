package com.nexus.app.domain.repository

import com.nexus.app.domain.model.NexusCapsule
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over capsule persistence.
 *
 * Capsules are immutable snapshots — only metadata (name, description) can
 * be edited after capture. Restoration is NOT part of Phase 5.
 */
interface CapsuleRepository {

    /** Observe all capsules. */
    fun observeAll(): Flow<List<NexusCapsule>>

    /** Observe a single capsule by ID. */
    fun observeById(id: String): Flow<NexusCapsule?>

    /** Get a single capsule by ID (non-reactive). */
    suspend fun getById(id: String): NexusCapsule?

    /**
     * Capture the current state of a Context into a new capsule.
     * Uses a Room transaction to ensure atomicity — no partial capsules.
     * Returns the ID of the created capsule.
     */
    suspend fun captureFromContext(contextId: String, name: String, description: String): String

    /** Update only the capsule's name. The snapshot remains unchanged. */
    suspend fun rename(id: String, newName: String)

    /** Update only the capsule's description. The snapshot remains unchanged. */
    suspend fun updateDescription(id: String, newDescription: String)

    /** Delete a capsule by ID. CASCADE removes snapshot children. Does NOT affect contexts. */
    suspend fun delete(id: String)

    /** Observe capsules for a specific source context. */
    fun observeForContext(contextId: String): Flow<List<NexusCapsule>>

    /** Count capsules for a specific source context. */
    fun observeCountForContext(contextId: String): Flow<Int>
}
