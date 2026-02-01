import Foundation
import shared

func log(
    level: LogLevel = .info,
    tag: String = "Munchies",
    message: String
) {
    LoggerKt.log(level: level, tag: tag, message: message)
}

func logInfo(tag: String = "Munchies", message: String) {
    LoggerKt.logInfo(tag: tag, message: message)
}

func logDebug(tag: String = "Munchies", message: String) {
    LoggerKt.logDebug(tag: tag, message: message)
}

func logError(tag: String = "Munchies", message: String) {
    LoggerKt.logError(tag: tag, message: message)
}
