package io.umain.munchies.feature.restaurant.navigation

import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RouteHandler

/**
 * Handler for the RestaurantList route.
 *
 * Manages the routing logic for displaying the list of restaurants.
 * This handler knows how to:
 * - Identify RestaurantList destinations
 * - Convert them to RestaurantListRoute instances
 * - Provide the platform-specific route string
 */
object RestaurantListRouteHandler : RouteHandler {
    override val route: Route = RestaurantListRoute()

    override fun toRouteString(): String = "restaurant_list"

    override fun canHandle(destination: Destination): Boolean =
        destination is Destination.RestaurantList

    override fun destinationToRoute(destination: Destination): Route? =
        if (canHandle(destination)) RestaurantListRoute() else null
}
