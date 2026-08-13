package com.nexus.app.domain.model.automation

/**
 * Optional condition that must be satisfied for an automation to execute.
 */
enum class AutomationCondition {
    /** No condition — always eligible. */
    NONE,
    /** The referenced context must currently be active. */
    CONTEXT_IS_ACTIVE,
    /** The configured app must currently be installed. */
    APP_IS_INSTALLED,
}
