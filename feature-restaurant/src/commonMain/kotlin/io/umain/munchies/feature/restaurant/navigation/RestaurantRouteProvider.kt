package io.umain.munchies.feature.restaurant.navigation

import io.umain.munchies.navigation.RouteHandler
import io.umain.munchies.navigation.RouteProvider

/**
 * Feature provider for the Restaurant feature.
 *
 * Declares all routes provided by the Restaurant feature to the app layer.
 * The app layer uses this to dynamically discover and register routes without
 * needing to know about specific route types.
 *
 * This enables features to own their routing logic completely, allowing new
 * features to be added without modifying the app layer.
 */
class RestaurantRouteProvider : RouteProvider {
    override fun getRoutes(): List<RouteHandler> = listOf(
        RestaurantListRouteHandler,
        RestaurantDetailRouteHandler
    )
}
