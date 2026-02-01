package io.umain.munchies.logging

import platform.Foundation.NSLog

actual object PlatformLogger {
    actual fun log(level: LogLevel, tag: String, message: String) {
        NSLog("[$tag] $message")
    }
}
