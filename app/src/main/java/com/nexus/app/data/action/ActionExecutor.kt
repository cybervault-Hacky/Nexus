package com.nexus.app.data.action

import com.nexus.app.domain.model.ActionPayload
import com.nexus.app.domain.model.ActionResult
import com.nexus.app.domain.model.ActionType
import com.nexus.app.domain.model.NexusAction

/**
 * Executes a single action by delegating to the appropriate handler.
 * Handlers are registered by action type.
 */
class ActionExecutor(
    private val handlers: Map<ActionType, ActionHandler>,
) {

    /** Execute a single action. */
    suspend fun execute(action: NexusAction): ActionResult {
        val startTime = System.currentTimeMillis()
        val handler = handlers[action.type]
            ?: return ActionResult.Failed(
                action.id, startTime, System.currentTimeMillis(),
                "No handler for action type: ${action.type}",
            )

        val payload = ActionPayload.fromJson(action.type, action.payload)
            ?: return ActionResult.Failed(
                action.id, startTime, System.currentTimeMillis(),
                "Invalid payload for action type: ${action.type}",
            )

        return handler.execute(action.id, payload, startTime)
    }
}
