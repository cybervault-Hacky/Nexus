package com.nexus.app.data.automation

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences-backed automation settings.
 * Simple and reliable — survives app restart and device reboot.
 */
class AutomationSettingsImpl(context: Context) : AutomationSettings {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nexus_automation_prefs", Context.MODE_PRIVATE)

    override suspend fun isGlobalEnabled(): Boolean =
        prefs.getBoolean(KEY_GLOBAL_ENABLED, true)

    override suspend fun isEnvironmentTriggersEnabled(): Boolean =
        prefs.getBoolean(KEY_ENV_TRIGGERS_ENABLED, true)

    override suspend fun isSourceEnabled(sourceId: String): Boolean =
        prefs.getBoolean("source_$sourceId", true)

    override suspend fun setGlobalEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GLOBAL_ENABLED, enabled).apply()
    }

    override suspend fun setEnvironmentTriggersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENV_TRIGGERS_ENABLED, enabled).apply()
    }

    override suspend fun setSourceEnabled(sourceId: String, enabled: Boolean) {
        prefs.edit().putBoolean("source_$sourceId", enabled).apply()
    }

    companion object {
        private const val KEY_GLOBAL_ENABLED = "global_automation_enabled"
        private const val KEY_ENV_TRIGGERS_ENABLED = "env_triggers_enabled"
    }
}
