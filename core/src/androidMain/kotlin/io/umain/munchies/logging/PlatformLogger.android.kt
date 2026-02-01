package io.umain.munchies.logging

import android.util.Log

actual object PlatformLogger {
    actual fun log(level: LogLevel, tag: String, message: String) {
        when (level) {
            LogLevel.INFO -> Log.i(tag, message)
            LogLevel.DEBUG -> Log.d(tag, message)
            LogLevel.ERROR -> Log.e(tag, message)
        }
    }
}
