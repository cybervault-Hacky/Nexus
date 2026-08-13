package com.nexus.app.data.action

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.nexus.app.domain.model.ActionPayload
import com.nexus.app.domain.model.ActionResult

/**
 * Handles OPEN_URL actions by launching an appropriate Android browser.
 */
class OpenUrlHandler(private val applicationContext: Context) : ActionHandler {

    override suspend fun execute(
        actionId: String,
        payload: ActionPayload,
        startTime: Long,
    ): ActionResult {
        val urlPayload = payload as? ActionPayload.OpenUrl
            ?: return ActionResult.Failed(actionId, startTime, System.currentTimeMillis(), "Invalid payload")

        val url = urlPayload.url
        if (url.isBlank()) {
            return ActionResult.Failed(actionId, startTime, System.currentTimeMillis(), "URL is empty")
        }

        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (intent.resolveActivity(applicationContext.packageManager) != null) {
                applicationContext.startActivity(intent)
                ActionResult.Success(actionId, startTime, System.currentTimeMillis())
            } else {
                ActionResult.Failed(actionId, startTime, System.currentTimeMillis(), "No app can handle this URL")
            }
        } catch (_: Exception) {
            ActionResult.Failed(actionId, startTime, System.currentTimeMillis(), "Failed to open URL")
        }
    }
}
