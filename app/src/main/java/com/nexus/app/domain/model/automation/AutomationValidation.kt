package com.nexus.app.domain.model.automation

import org.json.JSONObject

/**
 * Centralized validation for automation rules and trigger configurations.
 * Returns null on success, or an error message on failure.
 */
object AutomationValidation {

    const val MAX_NAME_LENGTH = 60
    const val MAX_DESCRIPTION_LENGTH = 200
    const val MAX_COOLDOWN_SECONDS = 86400 // 24 hours

    fun validateName(name: String): String? {
        if (name.isBlank()) return "Name cannot be empty"
        if (name.length > MAX_NAME_LENGTH) return "Name is too long (max $MAX_NAME_LENGTH characters)"
        return null
    }

    fun validateDescription(description: String): String? {
        if (description.length > MAX_DESCRIPTION_LENGTH)
            return "Description is too long (max $MAX_DESCRIPTION_LENGTH characters)"
        return null
    }

    fun validateCooldown(cooldownSeconds: Int): String? {
        if (cooldownSeconds < 0) return "Cooldown cannot be negative"
        if (cooldownSeconds > MAX_COOLDOWN_SECONDS) return "Cooldown cannot exceed 24 hours"
        return null
    }

    fun validateTriggerPayload(triggerType: TriggerType, payload: String): String? {
        return when (triggerType) {
            // Phase 7
            TriggerType.MANUAL -> null
            TriggerType.TIME -> validateTimePayload(payload)
            TriggerType.APP_OPEN, TriggerType.APP_CLOSE -> validateAppPayload(payload)
            TriggerType.CONTEXT_ACTIVATED -> validateContextPayload(payload)
            // Phase 8 — simple/no payload
            TriggerType.WIFI_CONNECTED -> null
            TriggerType.WIFI_DISCONNECTED -> null
            TriggerType.BLUETOOTH_CONNECTED -> null
            TriggerType.BLUETOOTH_DISCONNECTED -> null
            TriggerType.CHARGING_STARTED -> null
            TriggerType.CHARGING_STOPPED -> null
            TriggerType.BATTERY_BELOW, TriggerType.BATTERY_ABOVE -> validateBatteryPayload(payload)
            TriggerType.DEVICE_BOOT -> null
            TriggerType.SCREEN_ON -> null
            TriggerType.SCREEN_OFF -> null
            TriggerType.DEVICE_IDLE -> null
            TriggerType.DEVICE_ACTIVE -> null
        }
    }

    fun validate(contextId: String?, triggerType: TriggerType, name: String, description: String, cooldownSeconds: Int, payload: String): String? {
        return validateName(name)
            ?: validateDescription(description)
            ?: validateCooldown(cooldownSeconds)
            ?: validateTriggerPayload(triggerType, payload)
            ?: run {
                if (triggerType == TriggerType.CONTEXT_ACTIVATED && contextId.isNullOrBlank()) {
                    "Context must be selected"
                } else null
            }
    }

    private fun validateTimePayload(payload: String): String? {
        return try {
            val obj = JSONObject(payload)
            val hour = obj.optInt("hour", -1)
            val minute = obj.optInt("minute", -1)
            val days = obj.optInt("daysOfWeek", 0)
            if (hour < 0 || hour > 23) return "Hour must be 0–23"
            if (minute < 0 || minute > 59) return "Minute must be 0–59"
            if (days <= 0) return "At least one day must be selected"
            null
        } catch (_: Exception) {
            "Invalid time configuration"
        }
    }

    private fun validateAppPayload(payload: String): String? {
        return try {
            val obj = JSONObject(payload)
            val pkg = obj.optString("packageName", "")
            if (pkg.isBlank()) return "Package name cannot be empty"
            null
        } catch (_: Exception) {
            "Invalid app configuration"
        }
    }

    private fun validateContextPayload(payload: String): String? {
        return try {
            val obj = JSONObject(payload)
            val ctxId = obj.optString("contextId", "")
            if (ctxId.isBlank()) return "Context ID cannot be empty"
            null
        } catch (_: Exception) {
            "Invalid context configuration"
        }
    }

    private fun validateBatteryPayload(payload: String): String? {
        return try {
            val obj = JSONObject(payload)
            val threshold = obj.optInt("thresholdPercent", -1)
            if (threshold < 0 || threshold > 100) return "Threshold must be 0–100%"
            null
        } catch (_: Exception) {
            "Invalid battery configuration"
        }
    }
}
