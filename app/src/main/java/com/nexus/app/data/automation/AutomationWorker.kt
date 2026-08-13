package com.nexus.app.data.automation

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexus.app.NexusApplication
import com.nexus.app.domain.model.automation.TriggerEvent

/**
 * WorkManager worker that executes a TIME-triggered automation.
 * Loads the automation by ID, validates it, and invokes TriggerEngine.
 */
class AutomationWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val automationId = inputData.getString(AutomationScheduler.KEY_AUTOMATION_ID)
            ?: return Result.failure()

        val app = applicationContext as NexusApplication
        val engine = app.triggerEngine

        return try {
            val event = TriggerEvent.Time(automationId)
            engine.onTrigger(event)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
