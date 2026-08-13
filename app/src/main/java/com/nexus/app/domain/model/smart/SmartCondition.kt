package com.nexus.app.domain.model.smart

/**
 * Generalized condition types for smart automation.
 * Each condition can be evaluated against current device state.
 */
enum class ConditionType {
    CONTEXT_ACTIVE, APP_INSTALLED, APP_NOT_INSTALLED,
    TIME_BETWEEN, TIME_OUTSIDE, DAY_OF_WEEK, DATE_RANGE,
    BATTERY_ABOVE, BATTERY_BELOW, CHARGING, NOT_CHARGING,
    WIFI_CONNECTED, WIFI_DISCONNECTED,
    BLUETOOTH_CONNECTED, BLUETOOTH_DISCONNECTED,
    SCREEN_ON, SCREEN_OFF, DEVICE_IDLE, DEVICE_ACTIVE,
    LOCATION_INSIDE, LOCATION_OUTSIDE,
    CALENDAR_ACTIVE, CALENDAR_FREE,
    NOTIFICATION_PRESENT, CAPABILITY_AVAILABLE,
    AUTOMATION_ENABLED, AUTOMATION_DISABLED,
    EXECUTION_COUNT, LAST_EXECUTION_WITHIN,
}

/**
 * Operators for combining conditions.
 */
enum class ConditionOperator { ALL, ANY, NOT }

/**
 * A single condition or a composite group.
 * Serializable to/from JSON for Room storage.
 */
sealed class SmartCondition {
    /** Leaf condition with type and optional parameter. */
    data class Leaf(
        val type: ConditionType,
        val parameter: String = "",
        val description: String = "",
    ) : SmartCondition()

    /** Composite condition combining children with an operator. */
    data class Composite(
        val operator: ConditionOperator,
        val children: List<SmartCondition>,
    ) : SmartCondition()
}

/** Result of evaluating a single condition. */
enum class ConditionResult {
    PASS, FAIL, UNAVAILABLE, UNKNOWN,
}

/** Structured result of condition evaluation. */
data class ConditionEvaluationResult(
    val condition: SmartCondition,
    val result: ConditionResult,
    val explanation: String,
    val children: List<ConditionEvaluationResult> = emptyList(),
)
