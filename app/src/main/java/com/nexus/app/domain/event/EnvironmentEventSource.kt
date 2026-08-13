package com.nexus.app.domain.event

import com.nexus.app.domain.model.automation.TriggerEvent
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for an environment event source.
 * Each source monitors one aspect of the device environment
 * and produces [TriggerEvent]s.
 *
 * Implementations must:
 * - Be lifecycle-safe (start/stop)
 * - Not know about workflows or automations
 * - Only produce events
 * - Handle unsupported states gracefully
 */
interface EnvironmentEventSource {
    /** Unique identifier for this source. */
    val sourceId: String

    /** Human-readable name. */
    val displayName: String

    /** Whether this source is supported on the current device/API level. */
    fun isSupported(): Boolean

    /** Start monitoring. Call once. */
    fun start()

    /** Stop monitoring and release resources. */
    fun stop()

    /** Stream of events produced by this source. */
    fun events(): Flow<TriggerEvent>
}
