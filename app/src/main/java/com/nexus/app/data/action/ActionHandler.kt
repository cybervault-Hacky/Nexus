package com.nexus.app.data.action

import com.nexus.app.domain.model.ActionPayload
import com.nexus.app.domain.model.ActionResult

/**
 * Interface for action type handlers.
 * Each action type (OPEN_APP, OPEN_URL, DELAY) has its own handler.
 * New action types can be added by implementing this interface
 * without modifying existing handlers.
 */
interface ActionHandler {
    /** Execute the action and return a structured result. */
    suspend fun execute(
        actionId: String,
        payload: ActionPayload,
        startTime: Long,
    ): ActionResult
}
