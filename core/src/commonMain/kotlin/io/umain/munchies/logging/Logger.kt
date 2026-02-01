package io.umain.munchies.logging

/**
 * Log levels for the logging system
 */
enum class LogLevel {
    INFO,
    DEBUG,
    ERROR
}

/**
 * Platform-specific logger interface
 * Each platform implements its own logging mechanism
 */
expect object PlatformLogger {
    fun log(level: LogLevel, tag: String, message: String)
}

/**
 * Global logging function with convenient short name
 * Usage: log(LogLevel.INFO, "MyTag", "Message")
 * 
 * @param level The log level (INFO, DEBUG, ERROR)
 * @param tag The tag to identify the log source
 * @param message The log message
 */
fun log(level: LogLevel = LogLevel.INFO, tag: String = "Munchies", message: String) {
    val formattedMessage = formatLogMessage(level, tag, message)
    PlatformLogger.log(level, tag, formattedMessage)
}

/**
 * Convenience functions for specific log levels
 */
fun logInfo(tag: String = "Munchies", message: String) {
    log(LogLevel.INFO, tag, message)
}

fun logDebug(tag: String = "Munchies", message: String) {
    log(LogLevel.DEBUG, tag, message)
}

fun logError(tag: String = "Munchies", message: String) {
    log(LogLevel.ERROR, tag, message)
}

private fun formatLogMessage(level: LogLevel, @Suppress("UNUSED_PARAMETER") tag: String, message: String): String {
    return "[$level] $message"
}
