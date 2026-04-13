package io.umain.munchies.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Base sealed class for all routes in the app.
 *
 * Each route declares exactly which holder owns its ViewModel through the associated type.
 * This enables type-safe access to ViewModels without casting.
 *
 * Sealed class (not interface) allows polymorphic serialization with kotlinx.serialization.
 */
@Serializable
sealed class Route : Comparable<Route> {
    abstract val key: String
    @Transient open val isRootRoute: Boolean = false
    /** The Koin scope qualifier name used when creating the scope for this route. */
    @Transient open val scopeQualifier: String = "screen"

    override fun compareTo(other: Route): Int = key.compareTo(other.key)

    companion object {
        val rootRoutes: List<Route>
            get() = listOf(RestaurantListRoute())
    }
}
