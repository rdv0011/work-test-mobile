package io.umain.munchies.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class RestaurantListRoute(
    override val key: String = KEY
) : Route() {
    @Transient override val isRootRoute: Boolean = true
    @Transient override val scopeQualifier: String = "RestaurantListScope"

    companion object {
        const val KEY = "RestaurantList"
    }
}

@Serializable
data class RestaurantDetailRoute(
    val restaurantId: String
) : Route() {
    override val key: String = "${KEY_PREFIX}$restaurantId"
    @Transient override val isRootRoute: Boolean = false
    @Transient override val scopeQualifier: String = "RestaurantDetailScope"

    companion object {
        const val KEY_PREFIX = "RestaurantDetail_"
    }
}

@Serializable
data class SettingsRoute(
    override val key: String = KEY
) : Route() {
    @Transient override val isRootRoute: Boolean = true
    @Transient override val scopeQualifier: String = "screen"

    companion object {
        const val KEY = "Settings"
    }
}

fun Route.toDestination(): Destination? = when (this) {
    is RestaurantListRoute -> Destination.RestaurantList
    is RestaurantDetailRoute -> Destination.RestaurantDetail(restaurantId)
    is SettingsRoute -> Destination.Settings
    else -> null
}

