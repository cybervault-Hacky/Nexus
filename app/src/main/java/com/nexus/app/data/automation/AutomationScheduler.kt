package com.nexus.app.data.automation

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules TIME-based automations using WorkManager.
 * Uses unique work names per automation to avoid duplicates.
 */
class AutomationScheduler(private val context: Context) {

    fun scheduleTimeAutomation(automationId: String, hour: Int, minute: Int, daysOfWeek: Int) {
        val data = Data.Builder()
            .putString(KEY_AUTOMATION_ID, automationId)
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MINUTE, minute)
            .putInt(KEY_DAYS, daysOfWeek)
            .build()

        val request = PeriodicWorkRequestBuilder<AutomationWorker>(1, TimeUnit.DAYS)
            .setInputData(data)
            .setInitialDelay(calculateInitialDelay(hour, minute), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .addTag(TAG_AUTOMATION)
            .addTag("automation_$automationId")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "automation_$automationId",
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancelTimeAutomation(automationId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("automation_$automationId")
    }

    fun cancelAll() {
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG_AUTOMATION)
    }

    private fun calculateInitialDelay(targetHour: Int, targetMinute: Int): Long {
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, targetHour)
            set(java.util.Calendar.MINUTE, targetMinute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) target.add(java.util.Calendar.DAY_OF_MONTH, 1)
        return target.timeInMillis - now.timeInMillis
    }

    companion object {
        const val KEY_AUTOMATION_ID = "automation_id"
        const val KEY_HOUR = "hour"
        const val KEY_MINUTE = "minute"
        const val KEY_DAYS = "days"
        const val TAG_AUTOMATION = "nexus_automation"
    }
}
