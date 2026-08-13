package com.nexus.app.domain.event

/**
 * Represents the capability state of an environment event source.
 */
enum class EnvironmentCapability {
    /** Fully supported and available. */
    SUPPORTED,
    /** Supported but requires a runtime permission. */
    PERMISSION_REQUIRED,
    /** Permission was denied by the user. */
    PERMISSION_DENIED,
    /** Not supported on this device or Android version. */
    UNSUPPORTED,
    /** An error occurred while checking. */
    ERROR,
}
