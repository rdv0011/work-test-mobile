package io.umain.munchies.core.utils

import kotlin.random.Random

actual fun currentTimeMillis(): Long {
    return Random.nextLong(Long.MAX_VALUE)
}
