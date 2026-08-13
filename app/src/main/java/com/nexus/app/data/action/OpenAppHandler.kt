package com.nexus.app.data.action

import com.nexus.app.data.app.AppLauncher
import com.nexus.app.domain.model.ActionPayload
import com.nexus.app.domain.model.ActionResult

/**
 * Handles OPEN_APP actions by launching an installed Android application.
 */
class OpenAppHandler(private val appLauncher: AppLauncher) : ActionHandler {

    override suspend fun execute(
        actionId: String,
        payload: ActionPayload,
        startTime: Long,
    ): ActionResult {
        val appPayload = payload as? ActionPayload.OpenApp
            ?: return ActionResult.Failed(actionId, startTime, System.currentTimeMillis(), "Invalid payload")

        val packageName = appPayload.packageName
        if (packageName.isBlank()) {
            return ActionResult.Failed(actionId, startTime, System.currentTimeMillis(), "Package name is empty")
        }

        if (!appLauncher.isInstalled(packageName)) {
            return ActionResult.Failed(actionId, startTime, System.currentTimeMillis(), "App is not installed")
        }

        val launched = appLauncher.launch(packageName)
        val endTime = System.currentTimeMillis()

        return if (launched) {
            ActionResult.Success(actionId, startTime, endTime)
        } else {
            ActionResult.Failed(actionId, startTime, endTime, "Unable to launch app")
        }
    }
}
