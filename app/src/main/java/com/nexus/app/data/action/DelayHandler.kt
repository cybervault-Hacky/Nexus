package com.nexus.app.data.action

import com.nexus.app.domain.model.ActionPayload
import com.nexus.app.domain.model.ActionResult
import kotlinx.coroutines.delay

/**
 * Handles DELAY actions by suspending the coroutine for the specified duration.
 * Does NOT block the main thread.
 */
class DelayHandler : ActionHandler {

    override suspend fun execute(
        actionId: String,
        payload: ActionPayload,
        startTime: Long,
    ): ActionResult {
        val delayPayload = payload as? ActionPayload.Delay
            ?: return ActionResult.Failed(actionId, startTime, System.currentTimeMillis(), "Invalid payload")

        val durationMs = delayPayload.durationMs
        if (durationMs <= 0) {
            return ActionResult.Failed(actionId, startTime, System.currentTimeMillis(), "Duration must be positive")
        }

        delay(durationMs)
        return ActionResult.Success(actionId, startTime, System.currentTimeMillis())
    }
}
