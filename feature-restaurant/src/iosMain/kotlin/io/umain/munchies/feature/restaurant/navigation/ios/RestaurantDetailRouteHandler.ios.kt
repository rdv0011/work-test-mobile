package io.umain.munchies.feature.restaurant.navigation.ios

import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RouteHandler

/**
 * iOS-specific handler for the RestaurantDetail route.
 *
 * This is the Kotlin-side handler that's shared via KMP.
 * Handles parameterized route with restaurantId.
 *
 * Exposed as companion object for easier Swift access.
 */
object RestaurantDetailRouteHandlerImpl : RouteHandler {
    override val route: Route = RestaurantDetailRoute("")

    override fun toRouteString(): String = "restaurantDetail"

    override fun canHandle(destination: Destination): Boolean =
        destination is Destination.RestaurantDetail

    override fun destinationToRoute(destination: Destination): Route? {
        return (destination as? Destination.RestaurantDetail)?.let {
            RestaurantDetailRoute(it.restaurantId)
        }
    }
}
