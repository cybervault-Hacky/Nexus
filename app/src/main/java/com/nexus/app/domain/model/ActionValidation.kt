package com.nexus.app.domain.model

/**
 * Centralized validation for Actions and their payloads.
 * Returns null on success, or an error message string on failure.
 */
object ActionValidator {

    const val MAX_NAME_LENGTH = 60
    const val MAX_DESCRIPTION_LENGTH = 200
    const val MAX_DELAY_MS = 30_000L // 30 seconds
    private val ALLOWED_URL_SCHEMES = setOf("http", "https")

    /** Validate the action name. */
    fun validateName(name: String): String? {
        if (name.isBlank()) return "Name cannot be empty"
        if (name.length > MAX_NAME_LENGTH) return "Name is too long (max $MAX_NAME_LENGTH characters)"
        return null
    }

    /** Validate the action description (optional). */
    fun validateDescription(description: String): String? {
        if (description.length > MAX_DESCRIPTION_LENGTH) {
            return "Description is too long (max $MAX_DESCRIPTION_LENGTH characters)"
        }
        return null
    }

    /** Validate an action payload for the given type. */
    fun validatePayload(type: ActionType, payload: String): String? {
        val parsed = ActionPayload.fromJson(type, payload) ?: return "Invalid configuration"
        return when (parsed) {
            is ActionPayload.OpenApp -> {
                if (parsed.packageName.isBlank()) "Package name cannot be empty"
                else null
            }
            is ActionPayload.OpenUrl -> {
                if (parsed.url.isBlank()) return "URL cannot be empty"
                try {
                    val uri = java.net.URI(parsed.url)
                    if (uri.scheme == null) return "URL must include a scheme (http/https)"
                    if (uri.scheme.lowercase() !in ALLOWED_URL_SCHEMES) {
                        return "Only http and https URLs are supported"
                    }
                    null
                } catch (_: Exception) {
                    "Invalid URL format"
                }
            }
            is ActionPayload.Delay -> {
                if (parsed.durationMs <= 0) return "Duration must be positive"
                if (parsed.durationMs > MAX_DELAY_MS) return "Duration cannot exceed ${MAX_DELAY_MS / 1000} seconds"
                null
            }
        }
    }

    /** Validate the complete action (name + payload). */
    fun validate(name: String, description: String, type: ActionType, payload: String): String? {
        return validateName(name)
            ?: validateDescription(description)
            ?: validatePayload(type, payload)
    }
}
