package io.umain.munchies.feature.restaurant.navigation.ios

import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RouteHandler

/**
 * iOS-specific handler for the RestaurantList route.
 *
 * This is the Kotlin-side handler that's shared via KMP.
 * The actual view holder creation happens on the iOS side.
 *
 * Exposed as companion object for easier Swift access.
 */
object RestaurantListRouteHandlerImpl : RouteHandler {
    override val route: Route = RestaurantListRoute()

    override fun toRouteString(): String = "restaurantList"

    override fun canHandle(destination: Destination): Boolean =
        destination is Destination.RestaurantList

    override fun destinationToRoute(destination: Destination): Route? =
        if (canHandle(destination)) RestaurantListRoute() else null
}
