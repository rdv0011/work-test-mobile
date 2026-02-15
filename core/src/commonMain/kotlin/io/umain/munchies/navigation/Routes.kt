package io.umain.munchies.navigation

data class RestaurantListRoute(
    override val key: String = RestaurantListRoute.KEY
) : Route {
    override val isRootRoute: Boolean = true
    
    companion object {
        const val KEY = "RestaurantList"
    }
}

data class RestaurantDetailRoute(
    val restaurantId: String
) : Route {
    override val key: String = "${RestaurantDetailRoute.KEY_PREFIX}$restaurantId"
    override val isRootRoute: Boolean = false
    
    companion object {
        const val KEY_PREFIX = "RestaurantDetail_"
    }
}
