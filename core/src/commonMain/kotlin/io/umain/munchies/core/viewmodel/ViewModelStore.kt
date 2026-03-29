package io.umain.munchies.core.viewmodel

import kotlin.reflect.KClass

/**
 * Simple ViewModelStore for KMP shared ViewModels, keyed by a string (e.g., restaurantId).
 * Not thread-safe, for demo purposes. In production, use a thread-safe map or synchronize.
 */
object ViewModelStore {
    private val map = mutableMapOf<String, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrCreate(key: String, factory: () -> T): T {
        return map.getOrPut(key) { factory() } as T
    }

    fun remove(key: String) {
        map.remove(key)
    }

    fun <T : Any> get(key: String): T? = map[key] as? T

    fun clear() {
        map.clear()
    }
}
