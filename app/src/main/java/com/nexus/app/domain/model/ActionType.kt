package com.nexus.app.domain.model

/**
 * Supported action types.
 * Each type has its own payload schema and execution handler.
 * New types can be added by extending this enum and providing
 * a corresponding handler in the action execution layer.
 */
enum class ActionType {
    OPEN_APP,
    OPEN_URL,
    DELAY,
}
