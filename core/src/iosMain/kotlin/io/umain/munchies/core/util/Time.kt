package io.umain.munchies.core.util

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeMillis(): Long {
    val timestamp = NSDate().timeIntervalSince1970
    return (timestamp * 1000).toLong()
}
