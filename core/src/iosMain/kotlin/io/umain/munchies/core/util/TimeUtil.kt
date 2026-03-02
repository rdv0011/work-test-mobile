package io.umain.munchies.core.util

import kotlin.time.TimeSource

actual fun currentTimeMillis(): Long {
    return (TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds)
}
