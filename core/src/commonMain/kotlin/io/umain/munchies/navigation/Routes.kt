package io.umain.munchies.navigation

data class RestaurantListRoute(
    override val key: String = "RestaurantList"
) : Route

data class RestaurantDetailRoute(
    val restaurantId: String
) : Route {
    override val key: String = "RestaurantDetail_$restaurantId"
}
