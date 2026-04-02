package io.umain.munchies.navigation

data class RestaurantListRoute(
    override val key: String = KEY
) : Route {
    override val isRootRoute: Boolean = true
    override val scopeQualifier: String = "RestaurantListScope"

    companion object {
        const val KEY = "RestaurantList"
    }
}

data class RestaurantDetailRoute(
    val restaurantId: String
) : Route {
    override val key: String = "${KEY_PREFIX}$restaurantId"
    override val isRootRoute: Boolean = false
    override val scopeQualifier: String = "RestaurantDetailScope"

    companion object {
        const val KEY_PREFIX = "RestaurantDetail_"
    }
}

data class SettingsRoute(
    override val key: String = KEY
) : Route {
    override val isRootRoute: Boolean = true
    override val scopeQualifier: String = "screen"

    companion object {
        const val KEY = "Settings"
    }
}
