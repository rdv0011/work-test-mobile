package io.umain.munchies.core.localization

import platform.Foundation.NSBundle

actual fun stringResource(key: StringKey, vararg args: Any): String {
    val mainBundle = NSBundle.mainBundle
    val tableName: String? = null
    val localized = mainBundle.localizedStringForKey(key, tableName, "")
    
    return if (args.isEmpty()) {
        localized
    } else {
        try {
            var result = localized
            for (arg in args) {
                result = result.replaceFirstOccurrence("%@", arg.toString())
            }
            result
        } catch (e: Exception) {
            localized
        }
    }
}

actual fun pluralResource(key: StringKey, quantity: Int, vararg args: Any): String {
    val mainBundle = NSBundle.mainBundle
    val tableName: String? = null
    
    val pluralKey = when {
        quantity == 0 -> "${key}.zero"
        quantity == 1 -> "${key}.one"
        else -> "${key}.other"
    }
    
    val localized = mainBundle.localizedStringForKey(pluralKey, tableName, "")
    
    return if (args.isEmpty()) {
        localized
    } else {
        try {
            var result = localized
            for (arg in args) {
                result = result.replaceFirstOccurrence("%@", arg.toString())
            }
            result
        } catch (e: Exception) {
            localized
        }
    }
}

private fun String.replaceFirstOccurrence(pattern: String, replacement: String): String {
    val index = this.indexOf(pattern)
    return if (index >= 0) {
        this.substring(0, index) + replacement + this.substring(index + pattern.length)
    } else {
        this
    }
}
