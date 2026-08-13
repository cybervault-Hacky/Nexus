package com.nexus.app.domain.model.restore

/**
 * A single change entry in a restoration preview.
 */
data class RestoreChange(
    val category: ChangeCategory,
    val name: String,
    val detail: String,
    val type: RestoreChangeType,
)

/**
 * What kind of item this change refers to.
 */
enum class ChangeCategory {
    APP,
    ACTION,
    CONTEXT_NAME,
    CONTEXT_DESCRIPTION,
    CONTEXT_ICON,
    CONTEXT_COLOR,
}
