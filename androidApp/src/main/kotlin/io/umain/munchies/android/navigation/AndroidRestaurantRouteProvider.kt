package io.umain.munchies.android.navigation

import io.umain.munchies.android.features.restaurant.navigation.restaurantDetailRouteHandlerAndroid
import io.umain.munchies.android.features.restaurant.navigation.restaurantListRouteHandlerAndroid
import io.umain.munchies.navigation.RouteHandler
import io.umain.munchies.navigation.RouteProvider

class AndroidRestaurantRouteProvider : RouteProvider {
    override fun getRoutes(): List<RouteHandler> = listOf(
        restaurantListRouteHandlerAndroid(),
        restaurantDetailRouteHandlerAndroid()
    )
}
