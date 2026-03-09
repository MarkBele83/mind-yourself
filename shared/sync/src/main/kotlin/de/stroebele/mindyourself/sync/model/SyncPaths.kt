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

    // Message channels (fire-and-forget, not stored)
    const val MSG_SYNC_REQUEST = "/sync/request"
    const val MSG_SYNC_ACK = "/sync/ack"
}
