package io.umain.munchies.logging

import android.util.Log

actual object PlatformLogger {
    actual fun log(level: LogLevel, tag: String, message: String) {
        try {
            when (level) {
                LogLevel.INFO -> Log.i(tag, message)
                LogLevel.DEBUG -> Log.d(tag, message)
                LogLevel.ERROR -> Log.e(tag, message)
            }
        } catch (e: Exception) {
            // In unit tests, Log is not mocked and will throw RuntimeException
            // Silently ignore to allow tests to run without errors
            System.err.println("[$level] $tag: $message")
        }
    }
}
