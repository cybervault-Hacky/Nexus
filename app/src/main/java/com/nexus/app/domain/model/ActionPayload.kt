package com.nexus.app.domain.model

import org.json.JSONObject

/**
 * Typed representation of an Action's configuration payload.
 * Each action type maps to a specific payload variant.
 * Serialization/deserialization is centralized here.
 */
sealed class ActionPayload {

    abstract fun toJson(): String

    data class OpenApp(val packageName: String) : ActionPayload() {
        override fun toJson(): String = JSONObject().apply {
            put("packageName", packageName)
        }.toString()
    }

    data class OpenUrl(val url: String) : ActionPayload() {
        override fun toJson(): String = JSONObject().apply {
            put("url", url)
        }.toString()
    }

    data class Delay(val durationMs: Long) : ActionPayload() {
        override fun toJson(): String = JSONObject().apply {
            put("durationMs", durationMs)
        }.toString()
    }

    companion object {
        /** Parse a JSON payload string into a typed [ActionPayload]. */
        fun fromJson(type: ActionType, json: String): ActionPayload? {
            return try {
                val obj = JSONObject(json)
                when (type) {
                    ActionType.OPEN_APP -> OpenApp(
                        packageName = obj.getString("packageName"),
                    )
                    ActionType.OPEN_URL -> OpenUrl(
                        url = obj.getString("url"),
                    )
                    ActionType.DELAY -> Delay(
                        durationMs = obj.getLong("durationMs"),
                    )
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
