package de.stroebele.mindyourself.sync.model

/**
 * Data Layer API path constants.
 * Paths starting with '/' identify data items and message channels.
 */
object SyncPaths {
    // Phone → Watch: Reminder configuration
    const val REMINDER_CONFIGS = "/reminder_configs"

    // Watch → Phone: Log data for visualization
    const val HYDRATION_LOGS = "/hydration_logs"
    const val SUPPLEMENT_LOGS = "/supplement_logs"
    const val HEART_RATE_LOGS = "/heart_rate_logs"

    // Phone → Watch: Vacation mode settings
    const val VACATION_SETTINGS = "/vacation_settings"

    // Phone → Watch: Global app settings (e.g. step daily goal)
    const val APP_SETTINGS = "/app_settings"

    // Phone → Watch: Hydration logs sourced from Health Connect
    const val HC_HYDRATION_LOGS = "/hydration_hc_logs"

    // Message channels (fire-and-forget, not stored)
    const val MSG_SYNC_REQUEST = "/sync/request"
    const val MSG_SYNC_ACK = "/sync/ack"
}
